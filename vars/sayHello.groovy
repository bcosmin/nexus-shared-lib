#!/usr/bin/env groovy

//vars/sayHello.groovy
package com.nexus

import com.nexus.Logger

def call (String name = "World") {
    Logger.info("Hello, ${name}!")
}