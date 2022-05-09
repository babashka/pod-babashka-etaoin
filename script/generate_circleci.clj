#!/usr/bin/env bb

(ns generate-circleci
  (:require [clojure.string :as str]
            [flatland.ordered.map :refer [ordered-map]]
            ))

(def install-babashka
  {:run {:name "Install babashka"
         :command (str/join "\n" ["curl -sLO https://raw.githubusercontent.com/babashka/babashka/master/install"
                                  "chmod +x install"
                                  "sudo ./install"])}})

(def run-tests
  {:run {:name "Run tests"
         :command (str/join "\n" ["bb test"])}})

(def jvm
  (ordered-map :docker [{:image "circleci/clojure:openjdk-11-lein-browsers"}]
               :working_directory "~/repo"
               :environment (ordered-map :LEIN_ROOT "true")
               :steps ["checkout"
                       {:run {:name "Pull Submodules",
                              :command "git submodule init\ngit submodule update\n"}}
                       {:restore_cache {:keys ["jvm-{{ checksum \"project.clj\" }}-{{ checksum \".circleci/config.yml\" }}"]}}
                       {:run {:name "Install Clojure",
                              :command "
wget https://download.clojure.org/install/linux-install-1.10.3.1058.sh
chmod +x linux-install-1.10.3.1058.sh
sudo ./linux-install-1.10.3.1058.sh"}}
                       install-babashka
                       run-tests
                       {:save_cache {:paths ["~/.m2"],
                                     :key "jvm-{{ checksum \"project.clj\" }}-{{ checksum \".circleci/config.yml\" }}"}}]))

(def linux
  (ordered-map :docker [{:image "circleci/clojure:openjdk-11-lein-browsers"}]
               :working_directory "~/repo"
               :environment (ordered-map :LEIN_ROOT "true"
                                         :GRAALVM_HOME "/home/circleci/graalvm-ce-java11-21.3.0"
                                         :BABASHKA_PLATFORM "linux"
                                         :BABASHKA_XMX "-J-Xmx7g"
                                         :POD_TEST_ENV "native"
                                         :resource_class "large")
               :steps ["checkout"
                       {:run {:name "Pull Submodules",
                              :command "git submodule init\ngit submodule update\n"}}
                       {:restore_cache {:keys ["linux-{{ checksum \"project.clj\" }}-{{ checksum \".circleci/config.yml\" }}"]}}
                       {:run {:name "Install Clojure",
                              :command "
wget https://download.clojure.org/install/linux-install-1.10.3.1058.sh
chmod +x linux-install-1.10.3.1058.sh
sudo ./linux-install-1.10.3.1058.sh"}}
                       install-babashka
                       {:run {:name "Install lsof",
                              :command "sudo apt-get install lsof\n"}}
                       {:run {:name "Install native dev tools",
                              :command "sudo apt-get update\nsudo apt-get -y install gcc g++ zlib1g-dev\n"}}
                       {:run {:name "Download GraalVM",
                              :command "
cd ~
if ! [ -d graalvm-ce-java11-21.3.0 ]; then
  curl -O -sL https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.3.0/graalvm-ce-java11-linux-amd64-21.3.0.tar.gz
  tar xzf graalvm-ce-java11-linux-amd64-21.3.0.tar.gz
fi"}}
                       {:run {:name "Build binary",
                              :command "bb native-image",
                              :no_output_timeout "30m"}}
                       run-tests
                       {:run {:name "Release",
                              :command ".circleci/script/release\n"}}
                       {:save_cache {:paths ["~/.m2"
                                             "~/graalvm-ce-java11-21.3.0"],
                                     :key "linux-{{ checksum \"project.clj\" }}-{{ checksum \".circleci/config.yml\" }}"}}
                       {:store_artifacts {:path "/tmp/release",
                                          :destination "release"}}]))

(def mac
  (ordered-map :macos {:xcode "12.0.0"},
               :environment (ordered-map :GRAALVM_HOME "/Users/distiller/graalvm-ce-java11-21.3.0/Contents/Home",
                                         :BABASHKA_PLATFORM "macos",
                                         :BABASHKA_TEST_ENV "native",
                                         :BABASHKA_XMX "-J-Xmx7g"
                                         :POD_TEST_ENV "native"),
               :resource_class "large",
               :steps ["checkout"
                       {:run {:name "Pull Submodules",
                              :command "git submodule init\ngit submodule update\n"}}
                       {:restore_cache {:keys ["mac-{{ checksum \"project.clj\" }}-{{ checksum \".circleci/config.yml\" }}"]}}
                       {:run {:name "Install Clojure",
                              :command "script/install-clojure /usr/local\n"}}
                       {:run {:name "Install Leiningen",
                              :command "script/install-leiningen\n"}}
                       {:run {:name "Download GraalVM",
                              :command "
cd ~
ls -la
if ! [ -d graalvm-ce-java11-21.3.0 ]; then
  curl -O -sL https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.3.0/graalvm-ce-java11-darwin-amd64-21.3.0.tar.gz
  tar xzf graalvm-ce-java11-darwin-amd64-21.3.0.tar.gz
fi"}}
                       install-babashka
                       {:run {:name "Build binary",
                              :command "bb native-image",
                              :no_output_timeout "30m"}}
                       ;; tests are skipped, until we figure out how to install
                       ;; Firefox headless on CI
                       #_{:run {:name "Run tests",
                              :command "script/test\n"}}
                       {:run {:name "Release",
                              :command ".circleci/script/release\n"}}
                       {:save_cache {:paths ["~/.m2"
                                             "~/graalvm-ce-java11-21.3.0"],
                                     :key "mac-{{ checksum \"project.clj\" }}-{{ checksum \".circleci/config.yml\" }}"}}
                       {:store_artifacts {:path "/tmp/release",
                                          :destination "release"}}]))

(def config
  (ordered-map
   :version 2.1,
   :jobs (ordered-map
          :jvm jvm
          :linux linux
          :mac mac),
   :workflows (ordered-map
               :version 2
               :ci {:jobs ["jvm"
                           "linux"
                           "mac"]})))

(require '[clj-yaml.core :as yaml])
(spit ".circleci/config.yml"
      (str "# This file is generated by script/generate_circleci.clj. Please do not edit here.\n"
           (yaml/generate-string config :dumper-options {:flow-style :block})))
