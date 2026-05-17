def info*(String message) {
    echo "INFO: ${message}"
}

def warn*(String message) {
    echo "WARNING: ${message}"
}

def error*(String message) {
    echo "ERROR: ${message}"
}

def fatal*(String message) {
    echo "FATAL: ${message}"
}

def debug*(String message) {
    echo "DEBUG: ${message}"
}

def trace*(String message) {
    echo "TRACE: ${message}"
}