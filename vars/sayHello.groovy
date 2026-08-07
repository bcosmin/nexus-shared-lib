#!/usr/bin/env groovy

package com.nexus

import com.nexus.Logger

def call(String name = 'NexusSharedLib') {
    Logger.info("Hello, ${name}!")
}
