(ns yuubin.mail-test
  (:require [clojure.test :refer :all]
            [yuubin.mail :refer :all]))

(deftest test-format-for-mailgun
  (testing "set body as html"
    (let [message {"body" "<html><body><h1>Test</h1></body></html>"}
          formatted (format-for-mailgun message)]
      (is (not= message formatted))
      (is (= (get message "body") (get formatted "html")))))
  (testing "leave text and html fields alone"
    (let [message {"text" "A message for you!"}]
      (is (= message (format-for-mailgun message))))
    (let [message {"html" "A message for you!"}]
      (is (= message (format-for-mailgun message))))))

(deftest test-templates
  (testing "load known templates"
    (let [message {"template" "welcome.html"}
          formatted (format-for-mailgun message)]
      (is (not= message formatted))
      (is (not (nil? (get formatted "html"))))))
  (testing "ignore unknown templates"
    (let [message {"template" "bogus.html"}]
      (is (= (dissoc message "template") (format-for-mailgun message))))))
