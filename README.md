# lstn-dataflow

This project contains a dataflow job that is used to process files in Cloud Storage, and publishes them to PubSub.

## Setup your environment

If using VSCode, install [Language Support for Java by Red Hat](https://marketplace.visualstudio.com/items?itemName=redhat.java)

Ensure you have JDK Java 8 installed.

```bash
brew tap caskroom/versions
brew cask install java8
```

Then ensure you've exported $JAVA_HOME which is most likely:

```bash
/Library/Java/Home
```