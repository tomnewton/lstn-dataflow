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
export JAVA_HOME=/Library/Java/Home
```

## Deploying

The dataflow jobs here are published as [Dataflow Templates](https://cloud.google.com/dataflow/docs/templates/creating-templates).

Our circle.yml file will publish the templates using the following command:

```bash
 mvn compile exec:java \
     -Dexec.mainClass=in.lstn.Podcasts \
     -Dexec.args="--runner=DataflowRunner \
                  --project=lstn-in-dev \
                  --stagingLocation=gs://lstn-in-dev-dataflow/staging \
                  --output=gs://lstn-in-dev-dataflow/output \
                  --templateLocation=gs://lstn-in-dev-dataflow/templates/podcasts"
```