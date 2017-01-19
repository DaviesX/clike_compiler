#!/bin/bash

bin=dist/clikec.jar

i=0
for file in tst_scanner/*.crx
do
        echo "proccessing $file"
        let i=i+1;
        java -jar $bin $file > "tst_$i.tmp"
done

i=0
for file in tst_scanner/*.out
do
        echo "comparing $file"
        let i=i+1;
        diff $file "tst_$i.tmp"
done

i=0
for file in *.tmp
do
        let i=i+1;
        echo "removing tst_$i.tmp"
        rm "tst_$i.tmp"
done
