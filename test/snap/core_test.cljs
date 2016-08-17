(ns snap.core-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [snap.core :refer :all]))

(def remotes {:comment {:get "/comments/:id"}
              :comments {:list "/comments"
                         :post "/comments"
                         :put "/comments/:id"
                         :delete "/comments/:id"}})

(deftest test-build-http-requests
  (testing "get request with hardcoded :id"
    (let [app-state {:comment nil}
          new-state {:comment {:id 1}}
          params {:key :comment
                  :old-state app-state
                  :new-state new-state
                  :remotes remotes}
          reqs (build-http-requests params)]
      (is (= [{:url "/comments/:id" :method :get :body nil}])))))
