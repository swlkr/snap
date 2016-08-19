(ns snap.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [snap.core-test]))

(doo-tests 'snap.core-test)
