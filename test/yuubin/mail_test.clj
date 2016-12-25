(ns yuubin.mail-test
  (:require [clojure.test :refer :all]
            [yuubin.mail :refer :all]))

(def template-dir (.getFile (-> "templates" clojure.java.io/resource)))

(deftest test-format-for-mailgun
  (testing "set body as html"
    (let [message {"body" "<html><body><h1>Test</h1></body></html>"}
          formatted (format-for-mailgun message template-dir)]
      (is (not= message formatted))
      (is (= (get message "body") (get formatted "html")))))
  (testing "leave text and html fields alone"
    (let [message {"text" "A message for you!"}]
      (is (= message (format-for-mailgun message template-dir))))
    (let [message {"html" "A message for you!"}]
      (is (= message (format-for-mailgun message template-dir))))))

(deftest test-templates
  (testing "load known templates"
    (let [message {"template" "welcome.html"}
          formatted (format-for-mailgun message template-dir)]
      (is (not= message formatted))
      (is (not (nil? (get formatted "html"))))))
  (testing "fail on unknown templates"
    (let [message {"template" "bogus.html"}]
      (is (thrown? java.io.FileNotFoundException (format-for-mailgun message template-dir)))))
  (testing "using template-specific attributes"
    (is (clojure.string/includes?
          (load-template template-dir "welcome.html" {"t:name" "Rudy"})
          "Rudy"))
    (is (not (clojure.string/includes?
               (load-template template-dir "welcome.html" {})
               "%%")))))
