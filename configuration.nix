# Edit this configuration file to define what should be installed on
# your system.  Help is available in the configuration.nix(5) man page
# and in the NixOS manual (accessible by running ‘nixos-help’).

{ config, pkgs, ... }:
let
  unstable = import <nixos-unstable> { config = { allowUnfree = true; }; };
in 
{
  imports =
    [ # Include the results of the hardware scan.
      ./hardware-configuration.nix
    ];

  # Use the systemd-boot EFI boot loader.
  boot.loader.systemd-boot.enable = true;
  boot.loader.efi.canTouchEfiVariables = true;
  boot.loader.grub.useOSProber = true;

  networking.hostName = "schrofi-desktop"; # Define your hostname.
  #networking.wireless.enable = true;  # Enables wireless support via wpa_supplicant.
  networking.networkmanager = {
    enable = true;
    packages = with pkgs; [
      networkmanager-openvpn
    ];
  };

  # configuration for package installation
  nixpkgs.config = {
    allowUnfree = true;
    pulseaudio = true;
  };

  # The global useDHCP flag is deprecated, therefore explicitly set to false here.
  # Per-interface useDHCP will be mandatory in the future, so this generated config
  # replicates the default behaviour.
  networking.useDHCP = false;
  networking.interfaces.enp30s0.useDHCP = true;

  # Configure network proxy if necessary
  # networking.proxy.default = "http://user:password@proxy:port/";
  # networking.proxy.noProxy = "127.0.0.1,localhost,internal.domain";

  # Select internationalisation properties.
  # i18n.defaultLocale = "en_US.UTF-8";
  console = {
    font = "Lat2-Terminus16";
    keyMap = "de";
  };

  # Set your time zone.
  time.timeZone = "Europe/Vienna";

  fonts = {
    fonts = with pkgs; [
      fira
      fira-code
      fira-code-symbols
    ];
    fontconfig = {
      defaultFonts = {
        sansSerif = [ "Fira Sans" ];
        monospace = [ "Fira Code" ];
      };
    };
  };

  # List packages installed in system profile.
  environment.systemPackages = with pkgs; [
    unstable.androidStudioPackages.stable
    alacritty
    chezmoi
    chromium
    dbeaver
    emacs
    filezilla
    firefox
    fish
    flameshot
    galculator
    gimp
    git
    git-lfs
    gnupg
    gparted
    unstable.gradle
    htop
    i3
    jdk
    jmtpfs
    libreoffice
    mosh
    ncspot
    networkmanager
    ntfs3g
    openvpn
    pass
    pavucontrol
    pinentry
    postman
    pulseaudio
    ranger
    scrcpy
    thunderbird
    udevil
    unar
  ];  

  environment.variables = {
    EDITOR = "emacs";
    VISUAL = "emacs";
  };

  # Some programs need SUID wrappers, can be configured further or are
  # started in user sessions.
  programs.adb.enable = true;
  # programs.mtr.enable = true;
  programs.gnupg.agent = {
    enable = true;
  #   enableSSHSupport = true;
    pinentryFlavor = "gtk2";
  };

  # List services that you want to enable:

  # Enable the OpenSSH daemon.
  # services.openssh.enable = true;

  # Open ports in the firewall.
  # networking.firewall.allowedTCPPorts = [ ... ];
  # networking.firewall.allowedUDPPorts = [ ... ];
  # Or disable the firewall altogether.
  # networking.firewall.enable = false;

  # Enable CUPS to print documents.
  # services.printing.enable = true;

  # Enable sound.
  sound.enable = true;
  hardware.pulseaudio.enable = true;

  # Enable the X11 windowing system.
  services.xserver.enable = true;
  services.xserver.layout = "de";
  services.xserver.videoDrivers = [ "nvidia" ];
  # services.xserver.xkbOptions = "eurosign:e";

  # Enable touchpad support.
  # services.xserver.libinput.enable = true;

  # Enable the KDE Desktop Environment.
  # services.xserver.displayManager.sddm.enable = true;
  # services.xserver.desktopManager.plasma5.enable = true;
  services.xserver.windowManager.i3.enable = true;
  services.xserver.desktopManager.wallpaper.combineScreens = false;
  services.xserver.desktopManager.wallpaper.mode = "fill";

  # Define a user account. Don't forget to set a password with ‘passwd’.
  # users.users.jane = {
  #   isNormalUser = true;
  #   extraGroups = [ "wheel" ]; # Enable ‘sudo’ for the user.
  # };

  users.users.schrofi = {
    isNormalUser = true;
    home = "/home/schrofi";
    description = "Florian Schrofner";
    shell = pkgs.fish;
    extraGroups = ["wheel" "audio" "adbusers" "networkmanager"];
  };

  # This value determines the NixOS release from which the default
  # settings for stateful data, like file locations and database versions
  # on your system were taken. It‘s perfectly fine and recommended to leave
  # this value at the release version of the first install of this system.
  # Before changing this value read the documentation for this option
  # (e.g. man configuration.nix or on https://nixos.org/nixos/options.html).
  system.stateVersion = "20.03"; # Did you read the comment?
}