#!/usr/bin/bash
cat features.src
./mycc --ir features.src
./mycc -S features.src -o features.asm
./mycc features.src -o features
./features