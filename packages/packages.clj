#!/usr/bin/env bb

(require 
  '[babashka.fs :as fs]
  '[clojure.tools.cli :refer [parse-opts]]
  '[clojure.string :as string])

;;todo: import from base.clj
(defn- safe-sh [& commands]
  (as-> (apply shell/sh commands) $
    (if (= (:exit $) 0) $ (throw (Exception. (:err $))))))

(def cli-options
  [["-u" "--user USER" "The user to set up"]])

(def options (:options (parse-opts *command-line-args* cli-options)))
(def user (:user options))
(def home (str "/home/" user))

(def packages {
  :base ["bluez" "chromium" "emacs-gtk3" "fish-shell" "firefox" "flatpak" "git" "htop" "pass" "ranger" "thunar-archive-plugin" "wget" "xarchiver"]
  :work ["kotlin-bin" "scrcpy"]
})

(def flatpak-packages {
  :work ["com.getpostman.Postman" "com.slack.Slack"]
})

(def toolbox-link "https://download.jetbrains.com/toolbox/jetbrains-toolbox-2.1.1.18388.tar.gz")

;;todo: allow to pick specific package sets later

;;installing normal xbps packages
(println "installing packages...")
(let [packages-to-install (flatten (vals packages))]
  (apply safe-sh (concat ["xbps-install" "-y"] packages-to-install)))
(println "packages installed")

;;installing flatpak packages
(println "installing flatpak packages..")
(safe-sh "flatpak" "remote-add" "--if-not-exists" "flathub" "https://dl.flathub.org/repo/flathub.flatpakrepo")
(let [packages-to-install (flatten (vals flatpak-packages))]
  (apply safe-sh (concat ["flatpak" "install" "flathub" "--noninteractive"] packages-to-install)))
(println "flatpak packages installed")

;;installing jetbrains toolbox
(println "installing jetbrains toolbox..")
(let [download-file (str home "/Downloads/toolbox.tar.gz")
     target-directory (str home "/Applications/JetbrainsToolbox")]
  (safe-sh "wget" "-O" download-file toolbox-link)
  (fs/delete-tree target-directory)
  (fs/create-dirs target-directory)
  (fs/gunzip download-file target-directory {:replace-existing true})
  (fs/delete (fs/file download-file))
  (safe-sh "chown" (str user ":" user) "-R" target-directory))
(println "jetbrains toolbox installed")