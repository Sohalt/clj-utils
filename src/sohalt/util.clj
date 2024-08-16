(ns sohalt.util
  (:require [clojure.java.io :as io]
            [nrepl.server])
  (:import (java.nio.file Files)))

(defmacro with-nrepl-port
  "Writes `port` to .nrepl-port and deletes it after `body` completes.
  Remembers contents of an existing .nrepl-port file and restores it after `body` completes"
  [port & body]
  `(let [old-port# (try (slurp ".nrepl-port") (catch java.io.FileNotFoundException e# nil))]
     (spit ".nrepl-port" ~port)
     ~@body
     (if old-port#
       (spit ".nrepl-port" old-port#)
       (Files/deleteIfExists (.toPath (io/file ".nrepl-port"))))))

(defmacro with-nrepl
  "Starts an nREPL server, executes `body`, stops the nrepl server
  When the first argument is a map, it is passed as options to `nrepl.server/start-server`
  Writes and restores .nrepl-port

  (with-nrepl {:port 1234}
    (println \"nREPL running, press ENTER to quit\")
    (read-line))"
  [opts & body]
  `(with-open [nrepl-server# (nrepl.server/start-server ~(when (map? opts) opts))]
     (with-nrepl-port (:port nrepl-server#)
       ~(when-not (map? opts) opts)
       ~@body)))
