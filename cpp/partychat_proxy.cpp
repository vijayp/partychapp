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

#include "gloox/disco.h"
#include <time.h>
#include <stdio.h>
#include <locale.h>
#include <string>
#include <ext/hash_map>
#include <cstdio> // [s]print[f]

using namespace gloox;
using namespace __gnu_cxx;
using namespace std;
// for serializing hash_map

#define BOOST_HAS_HASH
#include <boost/serialization/hash_collections_load_imp.hpp>
#include <boost/serialization/hash_collections_save_imp.hpp>
#include <boost/serialization/optional.hpp>
#include <boost/archive/binary_oarchive.hpp>

#include <boost/serialization/hash_map.hpp>
#include <fstream>

namespace __gnu_cxx
{
        template<> struct hash< std::string >
        {
                size_t operator()( const std::string& x ) const
                {
                        return hash< const char* >()( x.c_str() );
                }
        };
}

///

class OneState {
public:
	enum State {
		UNKNOWN,
		PENDING,
		REJECTED,
		OK
	};
//private:
	time_t last_out_request_time_;
	State in_state_;
	State out_state_;
	bool first_time_;

public:
	OneState(int last_out_request_time,
		     State in_state,
		     State out_state,
		     bool first_time) :
		    	 last_out_request_time_(last_out_request_time),
		    	 		    	 in_state_(in_state),
		    	 		    	 out_state_(out_state),
		    	 		    	 first_time_(first_time) {}
    OneState() :
    	last_out_request_time_(0),
    	in_state_(UNKNOWN),
    	out_state_(UNKNOWN),
    	first_time_(true) {}
    static const int kReRequestTimeSec = 60*5;
	void ResetPendingIfPossible() {
		if (last_out_request_time_ + kReRequestTimeSec < time(NULL) ) {
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
typedef hash_map<string, UserStateMap>ChannelMap;

class SimpleProxy :
	public DiscoHandler, ConnectionListener, LogHandler, MessageHandler, PresenceHandler, SubscriptionHandler {
private:
		Component *component_;
	    ChannelMap channel_map_;
  public:
    SimpleProxy() {
    }
    virtual ~SimpleProxy() {}
    void start()
    {
      j = new Component( XMLNS_COMPONENT_ACCEPT, "localhost",
                         "component.localhost", "secret", 5000 );
      j->disco()->setVersion( "localhost", GLOOX_VERSION );

      j->registerConnectionListener( this );
      j->logInstance().registerLogHandler( LogLevelDebug, LogAreaAll, this );
      j->registerMessageHandler(this);
      j->registerSubscriptionHandler(this);
      j->registerPresenceHandler(this);


      component_ = j;
      j->connect();
    }

    virtual void onConnect()
    {
      printf( "connected -- disconnecting...\n" );
//       j->disconnect( STATE_DISCONNECTED );
    }

    virtual void onDisconnect( ConnectionError /*e*/ ) { printf( "component: disconnected\n" ); }

    virtual bool onTLSConnect( const CertInfo& info )
    {
      printf( "status: %d\nissuer: %s\npeer: %s\nprotocol: %s\nmac: %s\ncipher: %s\ncompression: %s\n",
              info.status, info.issuer.c_str(), info.server.c_str(),
              info.protocol.c_str(), info.mac.c_str(), info.cipher.c_str(),
              info.compression.c_str() );
      return true;
    }

    virtual void handleDiscoInfo( const JID& /*iq*/, const Disco::Info&, int /*context*/ )
    {
      printf( "handleDiscoInfoResult}\n" );
    }

    virtual void handleDiscoItems( const JID& /*iq*/, const Disco::Items&, int /*context*/ )
    {
      printf( "handleDiscoItemsResult\n" );
    }

    virtual void handleDiscoError( const JID& /*iq*/, const Error*, int /*context*/ )
    {
      printf( "handleDiscoError\n" );
    }

    virtual void handleLog( LogLevel level, LogArea area, const std::string& message )
    {
      printf("log: level: %d, area: %d, %s\n", level, area, message.c_str() );
    }

    virtual void handleMessage( const Message& msg, MessageSession* session = 0 ) {
    	/*if (msg.subtype() != Message::Chat) {
    		return;
    	}*/
    	printf("you said %s\n", msg.body().c_str());
    	Message nm(msg.subtype(), msg.from(),
        		msg.body(), "hello");
    	nm.setFrom(msg.to());

    	component_->send(nm);
    }

    virtual void handlePresence(const Presence& presence) {
    	switch(presence.presence()) {

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
        	string to = presence.to().bare();
    		printf("presence probe from %s\n", presence.from().bare().c_str());
    		SendPresence(to, from);
    		break;
    	}
    }
    virtual void SendPresence(const string& from, const string& to) {
    	Presence out(Presence::Available, to);
    	out.setFrom(from);
    	component_->send(out);
    	handleLog(gloox::LogLevelDebug, gloox::LogAreaClassComponent, "sent presence");

    }
    virtual void SendSubscribe(const string& from, const string& to) {
    	channel_map_[from][to].ResetPendingIfPossible();
    	if (channel_map_[from][to].out_state_ == OneState::UNKNOWN) {
    		channel_map_[from][to].SetOutboundRequest();
    		Subscription out(Subscription::Subscribe, to);
        		out.setFrom(from);
        		component_->send(out);
        	}
        }
    virtual void SendSubscribed(const string& from, const string& to) {
        	if ((channel_map_[from][to].in_state_ != OneState::OK)
        			&& (channel_map_[from][to].in_state_ != OneState::REJECTED)) {
        		channel_map_[from][to].in_state_ = OneState::OK;
        		Subscription out(Subscription::Subscribed, to);
        		out.setFrom(from);
        		component_->send(out);
        	}
        }

    virtual void handleSubscription(const Subscription& subscription) {
    	string from = subscription.from().bare();
    	string to = subscription.to().bare();
    	printf("Subscription from %s to %s\n", from.c_str(), to.c_str());
    	switch(subscription.subtype()) {
    	case Subscription::Invalid:
    		break;
    	case Subscription::Subscribed:
    		channel_map_[to][from].out_state_ = OneState::OK;
    	case Subscription::Subscribe:
    		SendSubscribed(to, from);
    		SendSubscribe(to,from);
    		break;
    	// TODO: fix this:
    	case Subscription::Unsubscribe:
    		break;
    	case Subscription::Unsubscribed:
    		break;
    	}
    }

  private:
    Component *j;
};

int main( int /*argc*/, char** /*argv*/ )
{
  SimpleProxy *r = new SimpleProxy();
  r->start();
  delete r;
  return 0;
}
