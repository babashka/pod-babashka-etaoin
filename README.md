> # pod-babashka-etaoin <sup>* *deprecated*</sup>
>
> **Great news! :tada:** [Etaoin](https://github.com/clj-commons/etaoin) is now babashka compatible!
This means that this pod is no longer necessary, and will no longer be maintained.
Please use [Etaoin](https://github.com/clj-commons/etaoin) instead.

[Babashka](https://github.com/borkdude/babashka)
[pod](https://github.com/babashka/babashka.pods) wrapping
[Etaoin](https://github.com/igrishaev/etaoin), a pure Clojure webdriver protocol
implementation.

This is work in progress. The API contains the most essential Etaoin functions,
but some may be missing, for which I will happily accept PRs.

## Install

- Use from the babashka registry as `(pods/load-pod 'org.babashka/etaoin "0.1.0")`
- Download a binary from Github releases

## Compatibility

This pod requires babashka v0.0.96 or later. Additionally you might have to
install `geckodriver` for Firefox, or `chromedriver` for Chrome.

## Namespaces

- `etaoin.api` is exposed as `pod.babashka.etaoin`
- `etaoin.keys` is exposed as `pod.babashka.etaoin.keys`
- `etaoin.query` is exposed as `pod.babashka.etaoin.query`

## Run

``` clojure
(require '[babashka.pods :as pods])
(pods/load-pod 'org.babashka/etaoin "0.1.0")
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

Run `bb native-image`

### Test

Run `bb test`.

## License

Copyright © 2020 - 2022 Michiel Borkent

Distributed under the EPL License. See LICENSE.
