# monix-nio

Java NIO utilities for usage with Monix

[![Build Status](https://travis-ci.org/monix/monix-nio.svg?branch=master)](https://travis-ci.org/monix/monix-nio)
[![Coverage Status](https://codecov.io/gh/monix/monix-nio/coverage.svg?branch=master)](https://codecov.io/gh/monix/monix-nio?branch=master)


We can talk under the Monix Gitter:
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/monix/monix?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Overview
Monix-nio can be used to have the power of Monix combined with underlying Java-nio libraries.
For the moment the following support has been added:
- Read/Write async to a file (combined with utf8 encoding/decoding if necessary)
- Read/Write async to TCP

## Usage
### Read from a text file

```scala
implicit val ctx = monix.execution.Scheduler.Implicits.global
val from = Paths.get(this.getClass.getResource("/myFile.txt").toURI)
monix.nio.file.readAsync(from, 30)
  .pipeThrough(utf8Decode)//decode utf8, If you need Array[Byte] just skip the decoding
  .foreach(Console.print(_))//print each char
```

### Write to a file
```scala
implicit val ctx = monix.execution.Scheduler.Implicits.global
val to = Paths.get("/out.txt")
val bytes = "Test String".getBytes.grouped(3)
Observable
  .fromIterator(bytes)
  .consumeWith(file.writeAsync(to))
  .runAsync
```
### Copy a file (text with decode and encode utf8)
```scala
val from = Paths.get("from.txt")
val to = Paths.get("to.txt")
val consumer = file.writeAsync(to)
readAsync(from, 3)
  .pipeThrough(utf8Decode)
  .map{ str =>
    Console.println(str) // do something with it
    str
  }
  .pipeThrough(utf8Encode)
  .consumeWith(consumer)
  .runAsync(callback)
```

### Read from TCP
```commandline
$echo 'monix-tcp' | nc -l -k 9000
```
```scala
import monix.execution.Scheduler.Implicits.global

val callback = new monix.eval.Callback[Unit] {
  override def onSuccess(value: Unit): Unit = println("Completed")
  override def onError(ex: Throwable): Unit = println(ex)
}
    
val reader = monix.nio.tcp.AsyncTcpClient.tcpReader("localhost", 9000)
reader
  .consumeWith(monix.reactive.Consumer.foreach(c => Console.out.print(new String(c))))
  .runAsync(callback)
```

### Write to TCP
```commandline
$nc -l -k 9000
```
```scala
import monix.execution.Scheduler.Implicits.global

val callback = new monix.eval.Callback[Long] {
  override def onSuccess(value: Long): Unit = println(s"Sent $value bytes")
  override def onError(ex: Throwable): Unit = println(ex)
}

val tcpConsumer = monix.nio.tcp.AsyncTcpClient.tcpWriter("localhost", 9000)
val chunkSize = 2
monix.reactive.Observable
  .fromIterator("monix-tcp".getBytes.grouped(chunkSize))
  .consumeWith(tcpConsumer)
  .runAsync(callback)
```

### Make a raw HTTP request
```commandline
$tail -f conn.txt | nc -l -k 9000 > conn.txt
```
```scala
import monix.execution.Scheduler.Implicits.global
  
val asyncTcpClient = 
  monix.nio.tcp.AsyncTcpClient("httpbin.org", 80, t => println(s"ERR $t")) // or use localhost:9000
  
val callbackR = new monix.eval.Callback[Unit] {
  override def onSuccess(value: Unit): Unit = println("OK")
  override def onError(ex: Throwable): Unit = println(ex)
}
asyncTcpClient
  .tcpObservable
  .map { reader =>
    reader.subscribe(
      (bytes: Array[Byte]) => {
        println(new String(bytes, "UTF-8"))
        monix.execution.Ack.Stop // closes the socket
      },
      err => println(err),
      () => println("Completed"))
    ()
  }
  .runAsync(callbackR)
  
  
val request = 
  "GET /get?tcp=monix HTTP/1.1\r\nHost: httpbin.org\r\nConnection: keep-alive\r\n\r\n"
  
val callbackW = new monix.eval.Callback[Long] {
  override def onSuccess(value: Long): Unit = println(s"Sent $value bytes")
  override def onError(ex: Throwable): Unit = println(ex)
}   
asyncTcpClient
  .tcpConsumer
  .flatMap { writer =>
    val data = request.getBytes("UTF-8").grouped(256 * 1024).toArray
    monix.reactive.Observable.fromIterable(data).consumeWith(writer)
  }
  .runAsync(callbackW)
```

The current maintainers (people who can help you) are:
- Sorin Chiprian ([@creyer](https://github.com/creyer))
- Alexandru Nedelcu ([@alexandru](https://github.com/alexandru))
- Radu Gancea ([@radusw](https://github.com/radusw))




