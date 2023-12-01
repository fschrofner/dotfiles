#!/usr/bin/env bb

;;todo: import from base.clj
(defn- safe-sh [& commands]
  (as-> (apply shell/sh commands) $
    (if (= (:exit $) 0) $ (throw (Exception. (:err $))))))

(def packages {
  :base ["bluez" "chromium" "emacs-gtk3" "fish-shell" "firefox" "flatpak" "git" "pass" "ranger"]
  :work ["kotlin-bin" "scrcpy"]
})

(def flatpak-packages {
  :work ["com.getpostman.Postman" "com.slack.Slack"]
})

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
