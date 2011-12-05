CXXFLAGS =	-O2 -g -Wall -fmessage-length=0 -static

OBJS =		partychat_proxy.o

LIBS = -lgloox -lpthread -I /usr/local/include/gloox -I /usr/local/lib

TARGET =	partychat_proxy

$(TARGET):	$(OBJS)
	$(CXX) -o $(TARGET) $(OBJS) $(LIBS)

all:	$(TARGET)

clean:
	rm -f $(OBJS) $(TARGET)
