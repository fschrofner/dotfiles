#!/bin/bash

# first install babashka which is required for the other scripts
xbps-install -y babashka;

# gets the path of this script
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

./packages/packages.clj;
./configs/configs.clj ${__dir};
