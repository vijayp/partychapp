line = '''I1007 21:14:30.750692 13590 partychat_proxy.cpp:379] outbound message the.exiles.mmtcp@im.partych.at <-  thalar.09@gmail.com mashweyi.mt@gmail.com khantkywesoe@gmail.com elninho.cyrus@gmail.com 1588161@gmail.com puge.puge@gmail.com poenaylynnaung@gmail.com unation.union@gmail.com bonelonetheee@gmail.com anesthetic31@gmail.com admin@w2pro.net appleak@appleak.net cesclinxoxo@gmail.com soulwatcher15@gmail.com zayzwh@gmail.com fattylay88@gmail.com citybountry@gmail.com anatazealot@gmail.com ahein.dr@gmail.com nemo.pk@gmail.com poohtutu@gmail.com kolynnlynn.tcplayer@gmail.com ye_htet@bod-mm.com phyusin.su@gmail.com kwikwi69@gmail.com tomorrow.of.hope@gmail.com hackermyothant.2009@gmail.com yankeelay87@gmail.com dr.sawmin@gmail.com aragorn.generation@gmail.com iamphyomaung@gmail.com zeyar.lin85@gmail.com 13.blackcat@gmail.com hitlernazi09@gmail.com thetnandaoo@gmail.com leonardo.set@gmail.com eaindranazi@gmail.com aungthawwin.cc@gmail.com mgchawlay@gmail.com santosa1.kyaw@gmail.com thaunghtet.soe@gmail.com achitsonelaymay@gmail.com alexzdj@gmail.com nyeinminthu91@googlemail.com strawberryplus@gmail.com gangdonor@gmail.com smartboy.lovely2@gmail.com gastovo@gmail.com aungaung.mmf@gmail.com nathan.ygn@gmail.com zealot2009@gmail.com yht2005@gmail.com alex.muzz.alex@gmail.com m.swam.95@gmail.com aungphonemyint899@gmail.com xiaohone@gmail.com mmsan01@gmail.com viperpink@gmail.com kyawkyaw.ygn85@gmail.com huichen.yin@gmail.com maintmaintt@gmail.com kwe.kwe12@gmail.com barjarmgmg@gmail.com boi.zenith@gmail.com paingminthu@gmail.com noruledevotee@gmail.com appleak@gmail.com'''

import time
import re

from collections import Counter, defaultdict

day_channel_map = defaultdict(Counter)
day_channel_map_unit = defaultdict(Counter)
day_user_map = defaultdict(Counter)
day_message_counter = defaultdict(Counter)
def match(line):
    m = re.match('I([0-9]+ +[0-9]+:[0-9]+:[0-9]+).[^[]*](.*)', line)
    if not m: return
    dt, message = m.groups()
    ts = time.strptime(dt, '%m%d %H:%M:%S')
    
    if 'outbound message' in message:
        outbound = True
    elif 'inbound message' in message:
        outbound = False
    else:
        return
    emails = re.findall('([^ ]*@[^ ]*)', message)
    channel = emails[0]
    users = emails[1:]
    day_str = ts[1:3]

    if outbound:
        day_channel_map[day_str][channel] += len(users)
        day_channel_map_unit[day_str][channel] += 1
        day_message_counter[day_str]['fanout'] += len(users)
        day_message_counter[day_str]['unit'] += 1
    else:
        for u in users:
           day_user_map[day_str][u] += 1
    
import sys
def print_counters(c, num=20):
    for kv in c.most_common(num):
        print '\t%s:%s' % kv

if __name__ == '__main__':
    for fn in sys.argv[1:]:
        for l in open(fn, 'r'):
            match(l)
    for d in day_channel_map:
        print ''.join(map(str,d))
        print '='*len(' '.join(map(str,d)))
        print_counters(day_channel_map_unit[d])
        print
        print_counters(day_channel_map[d])
        print
        print_counters(day_user_map[d])
        print
        print_counters(day_message_counter[d])

