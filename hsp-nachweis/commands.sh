#!/bin/bash
sed -i -e '3s/JAVA_OPTS/#JAVA_OPTS/' /etc/SBB/hsp-nachweis/hsp-nachweis.conf
sed -i -e '6s/#JAVA_OPTS/JAVA_OPTS/' /etc/SBB/hsp-nachweis/hsp-nachweis.conf
service hsp-nachweis restart
tail -f /var/log/SBB/hsp-nachweis/hsp-nachweis.log
