(ns snap.core)

(defn get-regex [str]
  (->> str
       (re-seq #":\w+")
       (clojure.string/join "|")
       (re-pattern)))

(defn first-if-set [val]
  (if (set? val)
    (first val)
    val))

(defn get-url [regex url diff]
  (->> diff
       (first-if-set)
       (map (fn [[k v]] [(str k) (str v)]))
       (into {})
       (clojure.string/replace url regex)))

(defn build-url [http-method url diff new-state]
  (let [regex (get-regex url)]
    (condp = http-method
      :put (get-url regex url diff)
      :post (get-url regex url diff)
      :delete (get-url regex url diff)
      :get (get-url regex url diff))))

(defn is-post? [diff state new-state]
  (let [n-state-len (count new-state)
        state-len (count state)]
    (and (not (nil? diff))
         (> (count diff) 0)
         (set? new-state)
         (set? state)
         (> n-state-len state-len))))

(defn is-delete? [diff state new-state]
  (let [n-state-len (count new-state)
        state-len (count state)]
    (and (not (nil? diff))
         (> (count diff) 0)
         (set? new-state)
         (set? state)
         (< n-state-len state-len))))

(defn is-put? [diff state new-state]
  (let [n-state-len (count new-state)
        state-len (count state)
        changed (filter identity diff)]
    (and (= n-state-len state-len)
         (set? new-state)
         (set? state)
         (> (count changed) 0))))

(defn is-get? [item]
  (not (set? item)))

(defn get-http-method [diff state new-state]
  (cond
    (is-put? diff state new-state) :put
    (is-post? diff state new-state) :post
    (is-delete? diff state new-state) :delete
    (is-get? new-state) :get
    :else nil))

(defn vec-to-set [val]
  (if (vector? val)
    (set val)
    val))

(defn get-sets [k old-state new-state]
  (->> (mapv #(get % k) [old-state new-state])
       (mapv vec-to-set)))

(defn get-diff [old new]
  (->> [old new]
       (sort-by count)
       (reverse)
       (apply clojure.set/difference)))

(defn get-body [http-method diff]
  (condp = http-method
    :put (first diff)
    :post (first diff)
    :delete nil
    :get nil))

(defn merge-sets [params]
  (let [{:keys [key old-state new-state]} params
        [old new] (get-sets key old-state new-state)]
    (merge {:old old :new new} params)))

(defn merge-diff [params]
  (let [{:keys [old new]} params
        d (get-diff old new)]
    (merge {:diff d} params)))

(defn merge-method [params]
  (let [{:keys [diff old new]} params
        method (get-http-method diff old new)]
    (if (not (nil? method))
      (merge {:method method} params)
      nil)))

(defn merge-remote [params]
  (let [{:keys [key remotes method]} params
        remote (-> key remotes method)]
    (merge {:remote remote} params)))

(defn merge-url [params]
  (let [{:keys [method remote diff new]} params
        url (build-url method remote diff new)]
    (merge {:url url} params)))

(defn merge-body [params]
  (let [{:keys [method diff]} params
        body (get-body method diff)]
    (merge {:body body} params)))

(defn build-http-request [params]
  (let [{:keys [k old-state new-state remotes]} params]
    (some-> params
            (merge-sets)
            (merge-diff)
            (merge-method)
            (merge-remote)
            (merge-url)
            (merge-body)
            (select-keys [:url :method :body]))))

(defn build-http-requests [old-state new-state remotes]
  (let [ks (keys remotes)]
    (mapv #(build-http-request {:old-state old-state :new-state new-state :remotes remotes :key %}) ks)))
