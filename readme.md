# schrofi's system configuration
My personal system configuration using cdist on a Manjaro system, specifically tailored towards me and nobody else ¯\_(ツ)_/¯
You can use it for inspiration though.

## Getting Started
1. Install cdist 
```
git clone https://code.ungleich.ch/ungleich-public/cdist.git
cd cdist
export PATH=$PATH:$(pwd -P)/bin
./bin/cdist-build-helper version
make install
sudo python setup.py install
```

2. Setup SSH root access
Add the following line into your `/etc/ssh/sshd_config`
```
PermitRootLogin without-password
```

Then copy your SSH key to the root user at localhost.
```
ssh-copy-id root@localhost
```

Finally, start the ssh daemon if it is not running
```
sudo systemctl start sshd
```

3. CD into this repository and execute the following command:
```
cdist config -vv -c ./cdist localhost
```
