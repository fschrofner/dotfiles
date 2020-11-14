# schrofi's system configuration
My personal system configuration using NixOS and chezmoi, specifically tailored towards me and nobody else ¯\_(ツ)_/¯
You can use it for inspiration though.

## Getting Started
1. Clone the repository into your /etc/nixos directory (delete/rename the existing configuration.nix).
2. Build the system with the cloned configuration, this will also create the "schrofi" user.
```
nixos-rebuild switch
```
3. Symlink the chezmoi config to the new users home dir.
```
mkdir -p ~/.config/chezmoi
ln -s /etc/nixos/chezmoi.json /home/schrofi/.config/chezmoi/chezmoi.json
```
4. Apply the configuration
```
chezmoi apply
```
5. Done
