snap
======================

Turns your global state atom changes into restful urls

Pull requests are welcome!

Usage
-----

In your `project.clj`, add the following dependency:

```clojure
[snap "0.1.0"]
```

Then, set up your remotes, global state and, oh snap! http requests from state changes!

```clojure
(ns foo
  (:require [snap.core :as snap]))

; Define the restful urls for each key in the global state atom
(def remotes {:posts {:get "/posts/:id"
                      :list "/posts"
                      :put "/posts/:id"
                      :post "/posts"
                      :delete "/posts/:id"}})

(def app-state {:posts [{:id 1 :title "Hodor" :content "Hodor hodor hodor"}]})
(def new-state (update-in app-state [:posts] conj {:id 2 :title "Hodor hodor" :content "Hodor"})

(snap/build-http-requests app-state new-state remotes)
; => [{:url "/posts" :method :post :body {:id 2 :title "Hodor hodor" :content "Hodor"}}]

```
