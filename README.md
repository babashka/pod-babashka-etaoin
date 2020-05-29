# pod-babashka-etaoin

[Babashka](https://github.com/borkdude/babashka) pod wrapping
[Etaoin](https://github.com/igrishaev/etaoin), a pure Clojure webdriver protocol
implementation.

This is work in progress.

<!-- ## Install -->

<!-- The following installation methods are available: -->

<!-- - Download a binary from Github releases -->
<!-- - With [brew](https://brew.sh/): `brew install borkdude/brew/pod-babashka-<db>` -->
<!-- where `<db>` must be substited with the database type, either `hsqldb` or -->
<!-- `postgresql`. -->

## Compatibility

Pods from this repo require babashka v0.0.96 or later.

## Run

The `etaoin.api` namespace is exposed as `pod.babashka.etaoin`.
The `etaoin.keys` namespace is exposed as `pod.babashka.etaoin.keys`.

``` clojure
(require '[babashka.pods :as pods])
(pods/load-pod "pod-babashka-etaoin")
(require '[pod.babashka.etaoin :as eta])
(def driver (eta/firefox))
(eta/go driver "https://clojure.org")
(eta/quit driver)
```

Also see [example.clj](example.clj).

## Dev

### Build

Run `script/compile`

### Test

Run `script/test`.

## License

Copyright © 2020 Michiel Borkent

Distributed under the EPL License. See LICENSE.
