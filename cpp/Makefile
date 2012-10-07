CXXFLAGS =	-O2 -g -Wall -fmessage-length=0 -static -I.

OBJS =		partychat_proxy.o jsoncpp.o

LIBS = -static-libgcc --static-libstdc++ -I /usr/local/include/gloox -I /usr/local/boostlibs/lib -I /usr/local/lib -lboost_serialization -lpion-net -lboost_system -lcurl -lboost_thread -lgloox -lpthread -lgflags -lglog 
TARGET =	partychat_proxy

$(TARGET):	$(OBJS)
	$(CXX) -o $(TARGET) $(OBJS) $(LIBS)

all:	$(TARGET)

clean:
	rm -f $(OBJS) $(TARGET)
