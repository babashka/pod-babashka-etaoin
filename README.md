# pod-babashka-etaoin

[Babashka](https://github.com/borkdude/babashka)
[pod](https://github.com/babashka/babashka.pods) wrapping
[Etaoin](https://github.com/igrishaev/etaoin), a pure Clojure webdriver protocol
implementation.

This is work in progress. The API contains the most essential Etaoin functions,
but some may be missing, for which I will happily accept PRs.

## Install

- Use from the babashka registry as `(pods/load-pod 'org.babashka/etaoin "0.0.3")`
- Download a binary from Github releases

## Compatibility

This pod requires babashka v0.0.96 or later. Additionally you might have to
install `geckodriver` for Firefox, or `chromedriver` for Chrome.

## Run

The `etaoin.api` namespace is exposed as `pod.babashka.etaoin`. The
`etaoin.keys` namespace is exposed as `pod.babashka.etaoin.keys`.

``` clojure
(require '[babashka.pods :as pods])
(pods/load-pod 'org.babashka/etaoin "0.0.3")
;; or for loading local binary: (pods/load-pod "./pod-babashka-etaoin")
(require '[pod.babashka.etaoin :as eta])
(def driver (eta/firefox))
(eta/go driver "https://clojure.org")
(eta/quit driver)
```

Also see [example.clj](example.clj):

``` clojure
$ bb example.clj
"https://en.wikipedia.org/wiki/Clojure"
"Clojure - Wikipedia"
true
"Clojure - Wikipedia"
```

## Dev

### Build

Run `script/compile`

### Test

Run `script/test`.

## License

Copyright © 2020 Michiel Borkent

Distributed under the EPL License. See LICENSE.
