#!/usr/bin/python

import argparse
import csv
import sys

from collections import Counter
parser = argparse.ArgumentParser(
    description='Analyse partychapp logs and estimate pricing for various params')
parser.add_argument('--price_per_xmpp', type=float, 
                    help='Price per xmpp message in currency units',
                    required=True)
parser.add_argument ('file')

parser.add_argument('--vary_max_roomsize', type=bool, default=True,
                    help='should we vary max_roomsize and generate a series of output lines?')
parser.add_argument('--csv', type=bool, default=False,
                    help='generate a csv file instead of a google chart url')
parser.add_argument('--max_roomsize', type=int, default=None,
                    help='truncate rooms larger than this many users')
parser.add_argument('--ignore_large_rooms', type=bool, default=False,
                    help='ignore rooms larger than this many users')

class SmartDictReader(csv.DictReader):
    def __init__(self, f, *args, **kwds):
        rdr = csv.reader(f, *args, **kwds)
        titles = rdr.next()
        csv.DictReader.__init__(self, f, titles, *args, **kwds)
    
def ReadCSVFile(filename, process):
    sdr = SmartDictReader(open(filename, 'r'))
    map(process, sdr)

def calculate_messages(rs_cnt_dict, size, ignore):
    msgs = 0
    for rs, cnt in rs_cnt_dict.items():
        if ignore and size < rs:
            continue
        msgs += min(rs, size) * cnt
    return msgs

def main(args):
    roomsize_message_count = Counter()
    def accum(dct):
        roomsize_message_count[int(dct['num_recipients'])] += 1
    ReadCSVFile(args.file, accum)
    
    if args.max_roomsize:
        sizes = [args.max_roomsize]
    else:
        sizes = sorted(roomsize_message_count.keys())

    points = []
    for size in sizes:
        points.append((size, 
                         (args.price_per_xmpp * 
                          calculate_messages(roomsize_message_count, 
                                            size, args.ignore_large_rooms)
                          )
                         ) )

    if args.csv:
        for p in points:
            print '%s,%s' % p
    else:
        x = [q[0] for q in points]
        y = [q[1] for q in points]
        from pygooglechart import XYLineChart, Axis
        chart = XYLineChart(400, 400)
        chart.add_data(x)
        chart.add_data(y)
#        chart.set_axis_labels(Axis.BOTTOM, 'max users/channel')
#        chart.set_axis_labels(Axis.LEFT, 'approx cost / week')
        print chart.get_url()+'&chxt=x,y'
        
    return 0

if __name__ == '__main__':
    args = parser.parse_args()
    assert args.vary_max_roomsize or args.max_roomsize
    print args
    sys.exit(main(args))
