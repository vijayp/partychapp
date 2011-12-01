
import argparse
parser = argparse.ArgumentParser()
parser.add_argument('--db_host', default='10.220.227.98')
parser.add_argument('--db_port', type=int, default=27017)
parser.add_argument('--db_collection', default='channel_state')
FLAGS = parser.parse_args()
conn = Connection(FLAGS.db_host, FLAGS.db_port)
db = self._conn[FLAGS.db_collection]
state_table = self._db['state']
state_table.drop()

import cPickle

data = cPickle.load(open('state.partychatproxy', 'r'))
state_table.create_index([('channel', ASCENDING), 
                          ('user', DESCENDING)],
                         unique=True)

for c, u, s in data:
    this_state = {'channel' : c, 'user' : u}
    this_state['in_state'] = 0
    this_state['out_state'] = 0
    this_state['message_received'] = False
    this_state['first_time'] = False
    this_state['last_out_request'] = 0
    idno = state_table.insert(this_state)


