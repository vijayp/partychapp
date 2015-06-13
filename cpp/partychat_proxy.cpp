#include "gloox/component.h"
#include "gloox/connectionlistener.h"
#include "gloox/message.h"
#include "gloox/messagehandler.h"
#include "gloox/presencehandler.h"
#include "gloox/presence.h"
#include "gloox/subscription.h"
#include "gloox/subscriptionhandler.h"
#include "gloox/loghandler.h"
#include "gloox/discohandler.h"
#include "gloox/vcard.h"
#include "json/json.h"
#include "gloox/disco.h"
#include <time.h>
#include <stdio.h>
#include <locale.h>
#include <curl/curl.h>
#include <string>
#include <sstream>
#include <sys/types.h>
#include <sys/resource.h>
#include <sys/time.h>
#include <unistd.h>
#include <pwd.h>
#include <ext/hash_map>
#include <cstdio> // [s]print[f]
#include <wchar.h>
#include <boost/threadpool.hpp>

using namespace gloox;
using namespace __gnu_cxx;
using namespace std;
// for serializing hash_mapu

#define BOOST_HAS_HASH
#include <boost/serialization/hash_collections_load_imp.hpp>
#include <boost/serialization/hash_collections_save_imp.hpp>
#include <boost/serialization/optional.hpp>
#include <boost/bind.hpp>
#include <boost/archive/binary_oarchive.hpp>
#include <boost/archive/binary_iarchive.hpp>
#include <boost/archive/text_oarchive.hpp>
#include <boost/archive/text_iarchive.hpp>

#include <boost/serialization/hash_map.hpp>
#include <fstream>

#include <gflags/gflags.h>
#include <glog/logging.h>


DEFINE_string(domain, "partych.at", "which high-level domain should we be running on (partych.at)");
DEFINE_string(component, "im", "which component within the high-level domain should we be running on?");
DEFINE_int32(xmpp_port, 5275, "what port should we connect to on the xmpp server");
DEFINE_int32(https_port, 443, "what port should we listen on for https traffic");
DEFINE_string(xmpp_secret, "secret", "xmpp secret");
DEFINE_string(default_token, "tokendata", "default xmpp token (if file cannot be read)");
DEFINE_string(token_file, "/etc/certs/token", "xmpp token file");
DEFINE_string(ssl_keyfile, "/etc/certs/server.all", "ssl key file");
DEFINE_string(state_file, "cppproxy.state", "default xmpp token");
DEFINE_string(appengine_hostname, "partychapp.appspot.com", "appengine hostname");
DEFINE_string(status_message, "", "status message for chat rooms");





// hash_map doesn't have a string hash for some reason
namespace __gnu_cxx {
template<> struct hash<std::string> {
    size_t operator()(const std::string& x) const {
      return hash<const char*>()(x.c_str());
    }
};
}
////

template<class T> void SaveState(T obj, const string& filename) {
  char *tmpl = strdup("tempXXXXXX");
  try {
    int outfile = mkstemp(tmpl);
    LOG(INFO) <<"saving state to " <<  filename << " via "<< tmpl;
    close(outfile);
    ofstream ofs(tmpl);
    //boost::archive::text_oarchive oa(ofs);
    boost::archive::binary_oarchive oa(ofs);
    oa << obj;
    rename(tmpl, filename.c_str());
  } catch (...) {
    LOG(ERROR)<< ("could not save state!\n");
  }
  free(tmpl);
  tmpl = NULL;
}

template<class T> bool LoadState(T* obj, const string& filename) {
  try {
    LOG(INFO) << "loading state from" << filename;
    ifstream ifs(filename.c_str());
    boost::archive::binary_iarchive ia(ifs);
    ia >> (*obj);
    LOG(INFO) << "loaded state data records: " << obj->size();
    return true;
  } catch (...) {
    LOG(ERROR)<< ("failed to load state data");
    return false;
  }
}

static string global_token;
// TODO: flag
static string kUrl;
//static const string kUrl = "http://localhost:8888/___control___";
///
using namespace boost::threadpool;
class OneState {
  public:
    enum State {
      UNKNOWN, PENDING, REJECTED, OK
    };
    //private:
    time_t last_out_request_time_;
    State in_state_;
    State out_state_;
    bool first_time_;

  public:
    OneState(int last_out_request_time, State in_state, State out_state,
        bool first_time) :
        last_out_request_time_(last_out_request_time), in_state_(in_state), out_state_(
            out_state), first_time_(first_time) {
    }
    OneState() :
        last_out_request_time_(0), in_state_(UNKNOWN), out_state_(UNKNOWN), first_time_(
            true) {
    }
    static const int kReRequestTimeSec = 60 * 5;
    void ResetPendingIfPossible() {
      if (out_state_ == PENDING
          && (last_out_request_time_ + kReRequestTimeSec < time(NULL))) {
        out_state_ = UNKNOWN;
      }
    }
    void SetOutboundRequest() {
      last_out_request_time_ = time(NULL);
    }
    template<class Archive>
    void serialize(Archive & ar, const unsigned int version) {
      ar & last_out_request_time_;
      ar & in_state_;
      ar & out_state_;
      ar & first_time_;
    }
};

typedef hash_map<string, OneState> UserStateMap;
typedef hash_map<string, UserStateMap> ChannelMap;
static const int kNumThreads = 20;
static const int kStateDumpTimeSeconds = 60 * 15;


static string kComponentDomain = "";

//static const string kComponentDomain = "component.localhost";
//static const string kDomain = "localhost";
class SimpleProxy: public DiscoHandler,
    ConnectionListener,
    LogHandler,
    MessageHandler,
    PresenceHandler,
    SubscriptionHandler {
  private:
    //TODO: these are leaked
    vector<Component*> components_;
    ChannelMap channel_map_;
    pool *threadpool_;
    string state_filename_;
    string token_;

    Component* random_component() {
      int s = rand();
      for (int i = 0; i < components_.size(); ++i) {
	if (components_[(s+i) % components_.size()]->authed()) {
	  return components_[(s+i) % components_.size()];
	}
      }
      LOG(ERROR) << ("BAD: no valid components found.."); // this should not happen
      return components_[0]; //default to the first component.
    }
  public:

    void LoopForever() {
      for (;;) {
        SaveState(channel_map_, state_filename_);
        sleep(kStateDumpTimeSeconds);
      }
    }
    static string ChannelName(const JID& jid) {
      return jid.username();
    }
    static size_t append(void* ptr, size_t size, size_t nmemb, void* ss) {
      stringstream *resp = static_cast<stringstream*>(ss);
      resp->write((char*) ptr, size * nmemb);
      return size * nmemb;
    }

    SimpleProxy() :
        threadpool_(new pool(kNumThreads)), state_filename_(
            FLAGS_state_file), token_(FLAGS_default_token) {
      LoadState(&channel_map_, state_filename_);
      threadpool_->schedule(boost::bind(&SimpleProxy::LoopForever, this));
      FILE * fp = fopen(FLAGS_token_file.c_str(), "r");
      if (fp) {
        char buffer[100];
        bzero(buffer, 100 * sizeof(char));
        const char* read = fgets(buffer, 99, fp);
        if (read) {
          for (int i = 0; i < strlen(buffer); ++i) {
            if (!isalnum(buffer[i])) {
              LOG(ERROR) << "ERROR BAD TOKEN CHARACTER " << i << ":" << buffer;
              goto tokendone;
            }
          }
	  LOG(INFO)<< "setting token to " << buffer;
          token_ = string(buffer);
        }
        tokendone:
        fclose(fp);
      } else {
        LOG(ERROR) << "COULD NOT OPEN TOKEN FILE. using default token";
      }
      global_token = token_;
    }

    virtual ~SimpleProxy() {
      delete threadpool_;
    }
    void start(const char* const hostname) {
      threadpool_->schedule(boost::bind(&SimpleProxy::blocking_start, this, hostname));
    }
    void blocking_start(const char* const hostname) {
      assert(hostname);
      LOG(INFO) << "Starting" << hostname;
      Component* component = new Component(XMLNS_COMPONENT_ACCEPT, hostname,
          kComponentDomain, FLAGS_xmpp_secret, FLAGS_xmpp_port);

      components_.push_back(component);

      component->disco()->setVersion(FLAGS_domain, GLOOX_VERSION);

      component->registerConnectionListener(this);
      component->logInstance().registerLogHandler(LogLevelWarning, LogAreaAll,
          this);
      component->registerMessageHandler(this);
      component->registerSubscriptionHandler(this);
      component->registerPresenceHandler(this);

      VCard* vcard = new VCard();
      (vcard)->setPhoto("http://partychapp.appspot.com/images/logo.png");
      vcard->setName("Partych.at beta", "");
      vcard->setNickname("partych.at");
      component->addPresenceExtension(vcard);

      for (;;) {
        component->connect();
	useconds_t sec = 1e7;
	LOG(INFO) << "sleeping for "<< sec <<" seconds";
	usleep(sec); // TODO: catch EINTR and try again with remaining time
      }

      delete vcard;
    }

    virtual void onConnect() {
      ;
    }
    virtual void onDisconnect(ConnectionError e) {
      printf("component: disconnected due to error\n");
    }

    void ProcessMessage(JID* from, JID* to, string* body) {
      LOG(INFO) << "inbound message "<< to->bare() << " <- " << from->bare();
      // 1. build json message
      Json::Value root;
      root["message_str"] = *body;
      root["to_str"] = to->bare();
      root["from_str"] = from->bare();
      root["state"] = "old";
      string json_out = root.toStyledString();


      CURL *curl;
      CURLcode res;

      // TODO: replace with http://pion.org/node/200

      // post does not work for some reason
      /* Fill in the token field
       struct curl_httppost *formpost=NULL;
       struct curl_httppost *lastptr=NULL;
       curl_formadd(&formpost,
       &lastptr,
       CURLFORM_COPYNAME, "token",
       CURLFORM_COPYCONTENTS, "tokendata",
       CURLFORM_END);
       /* Fill in the body field
       curl_formadd(&formpost,
       &lastptr,
       CURLFORM_COPYNAME, "body",
       CURLFORM_COPYCONTENTS, json_out.c_str(),
       CURLFORM_END);
       */

      curl = curl_easy_init();

      // TODO: auto_ptr
      char* json_esc = curl_easy_escape(curl, json_out.c_str(),
          json_out.size());
      // TODO: make token a command-line flag
      // TODO: filter out too-long strings
      string url_addition = "token="+ token_ +"&body=" + json_esc;
      string url = kUrl + "?" + url_addition;
      curl_free(json_esc);
      stringstream response;
      if (!curl) {
        goto done;
      }

      //      This crap is necessary because no one follows the 1.1 spec and sends a 100 on a POST
      //		static const char buf[] = "Expect:";
      //		  struct curl_slist *headerlist=NULL;
      //		  headerlist = curl_slist_append(headerlist, buf);

      //			  curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headerlist);
      //		curl_easy_setopt(curl, CURLOPT_HTTPPOST, formpost);
      
      //      curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
      curl_easy_setopt(curl, CURLOPT_URL, kUrl.c_str());
      curl_easy_setopt(curl, CURLOPT_POSTFIELDS, url_addition.c_str());
      curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);
      curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 0L);
      curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
      curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, SimpleProxy::append);

      res = curl_easy_perform(curl);
      curl_easy_cleanup(curl);
      //curl_formfree(formpost);
      ProcessJsonStream(response);
      done: delete from;
      delete to;
      delete body;
    }

    void ProcessJsonStream(stringstream& response) {
      for (;;) {
	
	Json::Value resp;
	try {
	  response >> resp;
	} catch (...) {
	  string str;
	  response >> str;
	  if (str.size()) {
	    LOG(ERROR) << "Could not parse json:"<< str;
	  }
	  return;
	}
	try {
	  string outbounds = "";
	  const string from_channel = resp["from_channel"].asString() + "@"
            + kComponentDomain;
	  const JID from_jid(from_channel);
	  //printf("Message; <%s>", resp["outmsg"].asCString());
	  Json::Value recs = resp["recipients"];
	  for (uint32_t i = 0; i < recs.size(); ++i) {
	    const string user_name = recs[i].asString();
	    OneState& state =
              channel_map_[resp["from_channel"].asString()][user_name];
	    //printf("states %d %d\n", state.in_state_, state.out_state_);
	    outbounds += " " + user_name;
	    Message nm(Message::Chat, JID(user_name), resp["outmsg"].asString());
	    nm.setFrom(from_jid);
	    random_component()->send(nm);
	    if (OneState::OK == state.in_state_
		&& OneState::OK == state.out_state_) {
	      ;
	    } else {
	      SendSubscribe(from_jid, JID(user_name), from_channel, user_name);
	      SendSubscribed(from_jid, JID(user_name), from_channel, user_name);

	    }
	  }
	  LOG(INFO) << "outbound message "<< from_channel << " <- " << outbounds;

	} catch (...) {
	  LOG(ERROR) << ("unknown error");
	  return;
	}
      }
    }

    virtual bool onTLSConnect(const CertInfo& info) {
      printf(
          "status: %d\nissuer: %s\npeer: %s\nprotocol: %s\nmac: %s\ncipher: %s\ncompression: %s\n",
          info.status, info.issuer.c_str(), info.server.c_str(),
          info.protocol.c_str(), info.mac.c_str(), info.cipher.c_str(),
          info.compression.c_str());
      return true;
    }

    virtual void handleDiscoInfo(const JID& /*iq*/, const Disco::Info&,
        int /*context*/) {
      printf("handleDiscoInfoResult}\n");
    }

    virtual void handleDiscoItems(const JID& /*iq*/, const Disco::Items&,
        int /*context*/) {
      printf("handleDiscoItemsResult\n");
    }

    virtual void handleDiscoError(const JID& /*iq*/, const Error*,
        int /*context*/) {
      printf("handleDiscoError\n");
    }

    virtual void handleLog(LogLevel level, LogArea area,
        const std::string& message) {
      LOG(INFO) << "level: " << level << " area:" << area << " message:" << message;
    }

    virtual void handleMessage(const Message& msg,
        MessageSession* session = 0) {
      if (msg.subtype() != Message::Chat) {
        return;
      }
      //printf("you said %s\n", msg.body().c_str());
      //bind(&X::f, ref(x), _1)(i);		// x.f(i)
      JID* from = new JID(msg.from());
      JID* to = new JID(msg.to());
      string* body = new string(msg.body());
      if (from->bare().find("im.partych.at") != string::npos) {
	LOG(ERROR) << "message loop!" << from->bare();
	goto bad_exit;
      } else if (body->empty()) {
	goto bad_exit;
      } else {
        threadpool_->schedule(
            boost::bind(&SimpleProxy::ProcessMessage, this, from, to, body));
      }
      return;

    bad_exit:
      delete from;
      delete to;
      delete body;

    }

    virtual void handlePresence(const Presence& presence) {
      switch (presence.presence()) {

        case Presence::Error:
        case Presence::Invalid:
        case Presence::Available:
        case Presence::Away:
        case Presence::Chat:
        case Presence::DND:
        case Presence::XA:
        case Presence::Unavailable:
          break;

        case Presence::Probe:
          // do something;
          string from = presence.from().bare();
          string to = presence.to().username();

          //printf("Presence %s -> %s \n", from.c_str(), to.c_str());
          SendPresence(presence.to().bareJID(), presence.from().bareJID(), to,
              from);
          break;
      }
    }
    virtual void SendPresence(const JID& from_jid, const JID& to_jid,
        const string& from = "", const string& to = "") {
      Presence out(Presence::Available, to_jid, FLAGS_status_message);
      out.setFrom(from_jid);
      
      random_component()->send(out);
      //printf("Presence %s -> %s \n", from.c_str(), to.c_str());
      //handleLog(gloox::LogLevelDebug, gloox::LogAreaClassComponent, "sent presence");

    }
    virtual void SendSubscribe(const JID& from_jid, const JID& to_jid,
        const string& from, const string& to) {
      channel_map_[from][to].ResetPendingIfPossible();
      if (channel_map_[from][to].out_state_ == OneState::UNKNOWN) {
        channel_map_[from][to].SetOutboundRequest();
        Subscription out(Subscription::Subscribe, to_jid);
        out.setFrom(from_jid);
        random_component()->send(out);
	
	

	//LOG(INFO) << "Subscribe " << from << "  " << to;
        channel_map_[from][to].out_state_ = OneState::PENDING;
      }
    }

    virtual void SendSubscribed(const JID& from_jid, const JID& to_jid,
        const string& from, const string& to) {
      if (((channel_map_[from][to].in_state_ != OneState::OK)
          && (channel_map_[from][to].in_state_ != OneState::REJECTED))) {
	//LOG(INFO) << "Subscribed " << from << " -> " << to;
        channel_map_[from][to].in_state_ = OneState::OK;
        Subscription out(Subscription::Subscribed, to_jid);
        out.setFrom(from_jid);
        random_component()->send(out);
      }
      SendPresence(from_jid, to_jid, from, to);
    }

    virtual void handleSubscription(const Subscription& subscription) {
      string from = subscription.from().bare();
      string to = subscription.to().username();

      switch (subscription.subtype()) {
        case Subscription::Invalid:
          break;
        case Subscription::Subscribed:
	  //	  LOG(INFO) << "Subscribed " << from << " -> " << to;
          channel_map_[to][from].out_state_ = OneState::OK;
          SendSubscribed(subscription.to().bareJID(),
              subscription.from().bareJID(), to, from);
          break;
        case Subscription::Subscribe:
	  //	  LOG(INFO) << "Subscribe " << from << " -> " << to;
          channel_map_[to][from].in_state_ = OneState::PENDING;
          SendSubscribed(subscription.to().bareJID(),
              subscription.from().bareJID(), to, from);
          SendSubscribe(subscription.to().bareJID(),
              subscription.from().bareJID(), to, from);
          break;

          // TODO: fix this:
        case Subscription::Unsubscribe:
          break;
        case Subscription::Unsubscribed:
          break;
      }
    }
};

/////////////// openssl locking

#include <pthread.h>
#include <openssl/crypto.h>
static pthread_mutex_t *lockarray;
static void lock_callback(int mode, int type, const char *file, int line) {
  if (mode & CRYPTO_LOCK) {
    pthread_mutex_lock(&(lockarray[type]));
  } else {
    pthread_mutex_unlock(&(lockarray[type]));
  }
}

static unsigned long thread_id(void) {
  unsigned long ret;

  ret = (unsigned long) pthread_self();
  return (ret);
}

static void init_locks(void) {
  int i;

  lockarray = (pthread_mutex_t *) OPENSSL_malloc(
      CRYPTO_num_locks() * sizeof(pthread_mutex_t));
  for (i = 0; i < CRYPTO_num_locks(); i++) {
    pthread_mutex_init(&(lockarray[i]), NULL);
  }

  CRYPTO_set_id_callback((unsigned long(*)())thread_id);CRYPTO_set_locking_callback
  (lock_callback);
}

static void kill_locks(void) {
  int i;

  CRYPTO_set_locking_callback(NULL);
  for (i = 0; i < CRYPTO_num_locks(); i++)
    pthread_mutex_destroy(&(lockarray[i]));

  OPENSSL_free(lockarray);
}

/////////////////
/// HTTP SERVER
#include <pion/net/WebService.hpp>
#include <pion/net/HTTPResponseWriter.hpp>
#include <pion/net/HTTPServer.hpp>
#include <pion/net/HTTPTypes.hpp>
#include <pion/net/HTTPRequest.hpp>
using namespace pion;
using namespace pion::net;
SimpleProxy* global_proxy_pointer;

void HandleRequest(HTTPRequestPtr& request, TCPConnectionPtr& tcp_conn) {

  static const std::string HELLO = "OK";
  HTTPResponseWriterPtr writer(
      HTTPResponseWriter::create(tcp_conn, *request,
          boost::bind(&TCPConnection::finish, tcp_conn)));
  HTTPResponse& r = writer->getResponse();
  HTTPTypes::QueryParams& params = request->getQueryParams();
  HTTPTypes::QueryParams::const_iterator body = params.find("body");
  HTTPTypes::QueryParams::const_iterator token = params.find("token");
  if (token != params.end() && body != params.end()
      && global_token == algo::url_decode(token->second)) {
    string in = algo::url_decode(body->second);
    stringstream tmp(in);
    LOG(INFO) << "post control-> me body: %s" << in;
    global_proxy_pointer->ProcessJsonStream(tmp);
    r.setStatusCode(HTTPTypes::RESPONSE_CODE_OK);
    writer->writeNoCopy(HELLO);
  } else {
    r.setStatusCode(HTTPTypes::RESPONSE_CODE_BAD_REQUEST);
  }
  writer->send();
}

/////////////////
int main(int argc, char** argv) {
  google::InitGoogleLogging(argv[0]);
  google::SetUsageMessage("Partychat Proxy");
  google::ParseCommandLineFlags(&argc, &argv, true);
  kComponentDomain = FLAGS_component + '.' + FLAGS_domain;
  LOG(INFO)<< "Hello";
  printf("domain: %s\n", FLAGS_domain.c_str());
  if (argc < 2) {
    printf("usage: %s jabber_hostname ....", argv[0]);
    return -1;
  }
  printf("Setting rlimit\n");
  struct rlimit limits;
  // 350 MB of RAM
  limits.rlim_cur = 1500 * (1 << 20);
  limits.rlim_max = 1500 * (1 << 20);
  CHECK(FLAGS_appengine_hostname.size());

  // TODO: check for bad characters in hostname
  kUrl = "https://" + FLAGS_appengine_hostname + "/___control___";
  LOG(INFO) << "setting hostname to " << kUrl;
  printf("\nHOSTNAME: %s\n", kUrl.c_str());

  int rval = setrlimit(RLIMIT_RSS, &limits);
  if (rval) {
    perror("could not set rlimit! \n");
  }
  rval = setrlimit(RLIMIT_AS, &limits);
  if (rval) {
    perror("could not set rlimit! \n");
  }

  curl_global_init(CURL_GLOBAL_ALL);
  init_locks();
  // TODO: redirect
  HTTPServerPtr hello_server(new HTTPServer(FLAGS_https_port));
  hello_server->setSSLKeyFile(FLAGS_ssl_keyfile.c_str());
  hello_server->setSSLFlag(true);
  hello_server->addResource("/___control___", &HandleRequest);
  hello_server->start();
  // drop permissions

  //TODO: leak?
  setuid(getpwnam("nobody")->pw_uid);
  SimpleProxy *r = global_proxy_pointer = new SimpleProxy();

  for (int i=1; i < argc; ++i){
    r->start(argv[i]);
  }

  delete r;
  kill_locks();
  return 0;
}
