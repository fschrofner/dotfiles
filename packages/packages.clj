#!/usr/bin/env bb

(require 
  '[babashka.fs :as fs])

;;todo: import from base.clj
(defn- safe-sh [& commands]
  (as-> (apply shell/sh commands) $
    (if (= (:exit $) 0) $ (throw (Exception. (:err $))))))

(def user (System/getenv "SUDO_USER"))
(def home (str "/home/" user))

(def packages {
  :base ["bluez" "chromium" "curl" "emacs-gtk3" "fish-shell" "firefox" "flameshot" "flatpak" "git" "htop" "i3" "pass" "ranger" "Signal-Desktop" "thunar-archive-plugin" "unzip" "wget" "xarchiver" "xclip" "xfce4-i3-workspaces-plugin"]
  :work ["kotlin-bin" "scrcpy"]
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


(def toolbox-link "https://download.jetbrains.com/toolbox/jetbrains-toolbox-2.1.1.18388.tar.gz")

(def http-toolkit-version "1.14.8")
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
(println "installing jetbrains toolbox..")
(let [target-directory (str home "/Applications/JetbrainsToolbox")]
  (install-application
   toolbox-link
   "toolbox.tar.gz"
   target-directory
   #(safe-sh "tar" "-xzf" %1 "-C" %2)))
(println "jetbrains toolbox installed")

;;installing http toolkit
(println "installing http toolkit..")
(let [target-directory (str home "/Applications/HttpToolkit")]
  (install-application
   http-toolkit-link
   "httptoolkit.zip"
   target-directory
   #(safe-sh "unzip" %1 "-d" %2)))
(println "http toolkit installed")
