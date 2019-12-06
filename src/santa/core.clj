(ns santa.core
  (:require [postal.core :as postal]
            [clojure.tools.logging :as log :refer [error]]))

(defn match [elves]
  (->> elves shuffle cycle (partition 2 1) (take (count elves))))

(defn send-email! [{:keys [email-config from]}
                   [[name email] [match match-email]]]
  (println "Sending email to" name)
  (try
    (postal/send-message email-config
                         {:from from
                          :to email
                          :subject "Your secret santa match"
                          :body (str "Hi " name
                                     ", your assigned person is "
                                     match "!")})
    (catch Exception e
      (error "EMAIL NOT SENT"))))

(defn pairs->str [pairs]
  (clojure.string/join "\n" (map #(clojure.string/join " -> " (map first %)) pairs)))

(defn send-review-email! [{:keys [email-config from] [name email] :reviewer}
                          pairs]
  (println "Sending review email to" name)
  (postal/send-message email-config
                       {:from from
                        :to email
                        :subject "Secret santa pairings"
                        :body (str "Hi "
                                   name
                                   " you are the reviewer! Here are the pairs:\n"
                                   (pairs->str pairs))}))

(defn load-config [file]
  (read-string (slurp file)))

(defn go! [config-file]
  (let [config (load-config config-file)
        pairs (match (:elves config))]
    (when (:send-to-reviewer? config) (send-review-email! config pairs))
    (when (:send-to-elves? config) (doseq [pair pairs] (send-email! config pair)))))
