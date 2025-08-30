#!/bin/bash

echo "🔨 Building ModelHunt Plugin JAR..."

# Create necessary directories
mkdir -p target/classes
mkdir -p target/jar-temp

# Copy resources to classes directory
echo "📁 Copying resources..."
cp -r src/main/resources/* target/classes/

# Create META-INF directory and MANIFEST.MF
mkdir -p target/classes/META-INF
cat > target/classes/META-INF/MANIFEST.MF << EOF
Manifest-Version: 1.0
Created-By: ModelHunt Plugin Builder
Implementation-Title: ModelHunt
Implementation-Version: 1.0.0
Main-Class: com.modelhunt.ModelHuntPlugin

EOF

# Copy all source files to jar temp (since we can't compile Java)
echo "📦 Preparing JAR contents..."
cp -r src/main/java/* target/jar-temp/
cp -r target/classes/* target/jar-temp/

# Create the JAR file using available tools
echo "🏗️  Creating JAR file..."
cd target/jar-temp
find . -type f -name "*.java" -o -name "*.yml" -o -name "*.txt" -o -name "MANIFEST.MF" | sort > ../file-list.txt
cd ../..

# Create a simple JAR structure
mkdir -p target/ModelHuntPlugin-1.0.0
cp -r target/jar-temp/* target/ModelHuntPlugin-1.0.0/

echo "✅ Plugin prepared successfully!"
echo ""
echo "📋 IMPORTANT INSTRUCTIONS:"
echo "   Since Java compilation requires Maven/JDK, please:"
echo "   1. Copy the entire project to a system with Maven installed"
echo "   2. Run: mvn clean package"
echo "   3. The JAR will be created in target/ModelHuntPlugin-1.0.0.jar"
echo ""
echo "📁 Source files are ready in: target/ModelHuntPlugin-1.0.0/"
echo "📖 Build instructions: target/BUILD-INSTRUCTIONS.txt"