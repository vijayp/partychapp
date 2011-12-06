CXXFLAGS =	-O2 -g -Wall -fmessage-length=0 -static -I.

OBJS =		partychat_proxy.o jsoncpp.o

LIBS = -lboost_system -lcurl -lboost_thread -lgloox -lpthread -I /usr/local/include/gloox -I /usr/local/boostlibs/lib

TARGET =	partychat_proxy

$(TARGET):	$(OBJS)
	$(CXX) -o $(TARGET) $(OBJS) $(LIBS)

all:	$(TARGET)

clean:
	rm -f $(OBJS) $(TARGET)
						