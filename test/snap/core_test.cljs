(ns snap.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [snap.core :refer [get-sets
                               get-diff
                               build-http-requests
                               get-http-method]]))

; test remotes that don't exist in old-state
; test remotes taht don't exist in new-state
; test remotes

(deftest test-get-sets
  (testing "get-sets without any vectors"
    (let [k :comments
          old-state {:comments nil}
          new-state {:comments {:id 1}}
          result (get-sets k old-state new-state)]
      (is (= [nil {:id 1}] result))))

  (testing "get-sets with some vectors"
    (let [k :comments
          old-state {:comments []}
          new-state {:comments [{:id 1 :content "comment"}]}
          result (get-sets k old-state new-state)]
      (is (= [#{} #{{:id 1 :content "comment"}} result])))))

(deftest test-get-diff
  (testing "with one set"
    (let [old #{}
          new #{{:id 1}}
          result (get-diff old new)]
      (is (= #{{:id 1} result}))))

  (testing "with no sets"
    (let [old nil
          new {:id 1}
          result (get-diff old new)]
      (is (= #{{:id 1}} result))))

  (testing "with deletion"
    (let [old #{{:id 1} {:id 2}}
          new #{{:id 2}}
          result (get-diff old new)]
      (is (= #{{:id 1} result}))))

  (testing "with an update not in a list"
    (let [old {:id 1 :t "t" :c "c"}
          new {:id 1 :t "title" :c "content"}
          result (get-diff old new)]
      (is (= result #{{:id 1 :t "title" :c "content"}})))))

(deftest test-get-http-method
  (testing "with a deletion"
    (let [diff #{{:id 1}}
          old #{{:id 1}}
          new #{}
          result (get-http-method diff old new)]
      (is (= :delete result))))

  (testing "with an addition"
    (let [diff #{{:id 1}}
          old #{}
          new #{{:id 1}}
          result (get-http-method diff old new)]
      (is (= :post result))))

  (testing "with a get"
    (let [diff nil
          old []
          new []
          result (get-http-method diff old new)]
      (is (= :get result))))

  (testing "with an update in a list"
    (let [diff #{{:id 1 :content "new content"}}
          old #{{:id 1 :content "content"}}
          new #{{:id 1 :content "new content"}}
          m (get-http-method diff old new)]
      (is (= :put m))))

  (testing "with an update not in a list"
    (let [diff {:id 1 :content "new content"}
          old {:id 1 :content "content"}
          new {:id 1 :content "new content"}
          m (get-http-method diff old new)]
      (is (= :put :put)))))

(deftest test-build-http-requests
  (testing "get request without params"
    (let [old-state {:comments []}
          new-state {:comments []}
          remotes {:comments {:get "/comments"}}
          reqs (build-http-requests old-state new-state remotes)]
      (is (= reqs [{:url "/comments" :method :get :body nil :key :comments}]))))

  (testing "get request with id param"
    (let [old-state {:comments nil}
          new-state {:comments {:id 1}}
          remotes {:comments {:get "/comments/:id"}}
          reqs (build-http-requests old-state new-state remotes)]
      (is (= reqs [{:url "/comments/1" :method :get :body nil :key :comments}]))))

  (testing "get request with other id param"
    (let [old-state {:comment nil}
          new-state {:comment {:post_id 1 :id 2}}
          remotes {:comment {:get "/posts/:post_id/comments/:id"}}
          reqs (build-http-requests old-state new-state remotes)]
      (is (= reqs [{:url "/posts/1/comments/2" :method :get :body nil :key :comment}]))))

  (testing "get request with other id param only"
    (let [old-state {:comments nil}
          new-state {:comments {:post_id 12}}
          remotes {:comments {:get "/posts/:post_id/comments"}}
          reqs (build-http-requests old-state new-state remotes)]
      (is (= reqs [{:url "/posts/12/comments" :method :get :body nil :key :comments}]))))

  (testing "when a remote doesn't exist in state"
    (let [old {:comment nil}
          new {:comment {:post_id 12}}
          r {:comments {:get "/posts/:post_id/comments"}}
          results (build-http-requests old new r)]
      (is (= results []))))

  (testing "when a state key doesn't exist in remotes"
    (let [old {:comment nil}
          new {:comment {:post_id 12}}
          r {:post {:get "/posts/:post_id"}}
          results (build-http-requests old new r)]
      (is (= results []))))

  (testing "post request"
    (let [o {:comments []}
          n {:comments [{:post_id 13 :id 1 :content "new comment"}]}
          r {:comments {:post "/posts/:post_id/comments"}}
          results (build-http-requests o n r)]
      (is (= results [{:url "/posts/13/comments" :method :post :body {:post_id 13 :id 1 :content "new comment"} :key :comments}]))))

  (testing "put request without a list"
    (let [o {:r {:id 1 :title "t" :content "c"}}
          n {:r {:id 1 :title "title" :content "content"}}
          r {:r {:put "/posts/:id"}}
          results (build-http-requests o n r)]
      (is (= results [{:key :r :url "/posts/1" :method :put :body {:id 1 :title "title" :content "content"}}]))))

  (testing "put request with a vec"
    (let [o {:r [{:id 1 :title "t" :content "c"}]}
          n {:r [{:id 1 :title "title" :content "content"}]}
          r {:r {:put "/posts/:id"}}
          results (build-http-requests o n r)]
      (is (= results [{:key :r :url "/posts/1" :method :put :body {:id 1 :title "title" :content "content"}}])))))
