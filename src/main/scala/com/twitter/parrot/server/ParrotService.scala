/*
Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.twitter.parrot.server

import com.twitter.finagle.Service
import com.twitter.parrot.config.ParrotServerConfig
import com.twitter.parrot.thrift.{ParrotJob, TargetHost}
import com.twitter.util.Future
import java.util.concurrent.atomic.AtomicReference

class ParrotService[Req <: ParrotRequest, Rep](config: ParrotServerConfig[Req, Rep]) extends Service[Req, Rep] {
  lazy val queue = config.queue.getOrElse(throw new Exception("Unconfigured request queue"))

  val jobRef = new AtomicReference[ParrotJob]()

  def apply(req: Req): Future[Rep] = {
    queue.addRequest(jobRef.get, req)
  }

  def setJob(job: ParrotJob) {
    jobRef.set(job)
  }

  def registerDefaultProcessors() { }

  def chooseRandomVictim: TargetHost = {
    val job = jobRef.get
    job.victims.get(config.randomizer.nextInt(job.victims.size))
  }
}
