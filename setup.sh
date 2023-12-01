#!/bin/sh

# first install babashka which is required for the other scripts
xbps-install -y babashka;

./packages/packages.clj;
./configs/configs.clj;