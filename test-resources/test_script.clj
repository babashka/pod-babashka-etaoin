(require '[babashka.pods :as pods])

(pods/load-pod ["lein" "run" "-m" "pod.babashka.etaoin"])
#_(pods/load-pod "./pod-babashka-etaoin")
(require '[pod.babashka.etaoin :as eta]
         '[pod.babashka.etaoin.keys :as k])

#_(def driver (eta/firefox))  ;; here, a Firefox window should appear
#_(def driver (eta/chrome))
(def driver (eta/firefox-headless))

(eta/go driver "https://en.wikipedia.org/")
(eta/wait-visible driver [{:id :simpleSearch} {:tag :input :name :search}])

;; screenshot
#_(eta/screenshot-element driver [{:tag :body}] "/tmp/screenshot.png")

(eta/quit driver)
nil
