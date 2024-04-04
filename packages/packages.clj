#!/usr/bin/env bb

(require 
  '[babashka.fs :as fs])

;;todo: import from base.clj
(defn- safe-sh [& commands]
  (as-> (apply shell/sh commands) $
    (if (= (:exit $) 0) $ (throw (Exception. (:err $))))))

(def user (System/getenv "SUDO_USER"))
(def home (str "/home/" user))

(safe-sh "xbps-install" "-Sy" "void-repo-nonfree")
(safe-sh "xbps-install" "-Sy" "void-repo-multilib")
(safe-sh "xbps-install" "-Sy" "void-repo-multilib-nonfree")
(safe-sh "xbps-install" "-S")

(def packages {
               :base ["alsa-firmware" "avahi" "bluez" "chromium" "clojure" "curl" "cups" "cups-filters" "emacs-gtk3" "engrampa" "feh" "fish-shell" "firefox" "flameshot" "flatpak" "font-firacode" "foomatic-db" "foomatic-db-nonfree" "gimp" "git" "git-annex" "gparted" "guvcview" "htop" "intel-ucode" "inxi" "i3" "i3status" "libgcc-32bit" "libstdc++-32bit" "libdrm-32bit" "libglvnd-32bit" "mate" "mate-extra" "mate-power-manager" "mate-terminal" "mate-utils" "mtools" "nss-mdns" "pass" "ranger" "rofi" "Signal-Desktop" "sof-firmware" "sox" "ssr" "steam" "sysfsutils" "the_silver_searcher" "thunar-archive-plugin" "udevil" "unzip" "wget" "xbindkeys" "xclip" "xz"]
               :work ["android-udev-rules" "filezilla" "git-lfs" "kotlin-bin" "libcxx" "libcxxabi" "scrcpy"]
               :intel ["mesa-intel-dri" "libva-intel-driver" "mesa-dri-32bit"]
               :amd ["amdvlk" "amdvlk-32bit" "mesa-dri-32bit" "mesa-vulkan-radeon" "mesa-vaapi" "mesa-vdpau" "vulkan-loader"]
})

(def flatpak-packages {
  :work ["com.getpostman.Postman" "com.slack.Slack"]
  :game ["com.discordapp.Discord"]
})

;;todo: allow to pick specific package sets later

;;installing normal xbps packages
(println "installing packages..")
(let [packages-to-install (flatten (vals packages))]
  (apply safe-sh (concat ["xbps-install" "-y"] packages-to-install)))
(println "packages installed")

;;installing flatpak packages
(println "installing flatpak packages..")
(safe-sh "flatpak" "remote-add" "--if-not-exists" "flathub" "https://dl.flathub.org/repo/flathub.flatpakrepo")
(let [packages-to-install (flatten (vals flatpak-packages))]
  (apply safe-sh (concat ["flatpak" "install" "flathub" "--noninteractive"] packages-to-install)))
(println "flatpak packages installed")


(def toolbox-link "https://download-cdn.jetbrains.com/toolbox/jetbrains-toolbox-2.2.3.20090.tar.gz")

(def http-toolkit-version "1.14.11")
(def http-toolkit-link (str "https://github.com/httptoolkit/httptoolkit-desktop/releases/download/v" http-toolkit-version "/HttpToolkit-linux-x64-" http-toolkit-version ".zip"))

(defn- install-application [link filename target-directory extract-command]
  (let [download-file-path (str home "/Downloads/" filename)
        download-file (fs/file download-file-path)]
    (safe-sh "wget" "-O" download-file-path link)
    (fs/delete-tree target-directory)
    (fs/create-dirs target-directory)
    (extract-command download-file-path target-directory)
    (fs/delete download-file)
    (safe-sh "chown" (str user ":" user) "-R" target-directory)))

;;installing jetbrains toolbox
(defn- install-jetbrains-toolbox []
 (println "installing jetbrains toolbox..")
(let [target-directory (str home "/Applications/JetbrainsToolbox")]
  (install-application
   toolbox-link
   "toolbox.tar.gz"
   target-directory
   #(safe-sh "tar" "-xzf" %1 "-C" %2)))
(println "jetbrains toolbox installed"))

;;installing http toolkit
(defn- install-http-toolkit []
  (println "installing http toolkit..")
  (let [target-directory (str home "/Applications/HttpToolkit")]
  (install-application
   http-toolkit-link
   "httptoolkit.zip"
   target-directory
   #(safe-sh "unzip" %1 "-d" %2)))
(println "http toolkit installed"))

(let [applications-dir (str home "/Applications")]
  (fs/create-dirs applications-dir)
  (safe-sh "chown" (str user ":" user) "-R" applications-dir))

(install-jetbrains-toolbox)
(install-http-toolkit)
