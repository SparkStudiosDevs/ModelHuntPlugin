const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('Building ModelHunt Plugin...');

// Create target directory
const targetDir = 'target';
if (!fs.existsSync(targetDir)) {
    fs.mkdirSync(targetDir);
}

// Create classes directory
const classesDir = path.join(targetDir, 'classes');
if (!fs.existsSync(classesDir)) {
    fs.mkdirSync(classesDir, { recursive: true });
}

// Copy resources
const resourcesDir = 'src/main/resources';
const targetResourcesDir = path.join(classesDir);

function copyDir(src, dest) {
    if (!fs.existsSync(src)) return;
    
    const entries = fs.readdirSync(src, { withFileTypes: true });
    
    for (const entry of entries) {
        const srcPath = path.join(src, entry.name);
        const destPath = path.join(dest, entry.name);
        
        if (entry.isDirectory()) {
            if (!fs.existsSync(destPath)) {
                fs.mkdirSync(destPath, { recursive: true });
            }
            copyDir(srcPath, destPath);
        } else {
            fs.copyFileSync(srcPath, destPath);
        }
    }
}

console.log('Copying resources...');
copyDir(resourcesDir, targetResourcesDir);

// Create JAR structure
const jarDir = path.join(targetDir, 'jar-contents');
if (!fs.existsSync(jarDir)) {
    fs.mkdirSync(jarDir, { recursive: true });
}

// Copy all contents to jar directory
copyDir(classesDir, jarDir);

// Create META-INF directory
const metaInfDir = path.join(jarDir, 'META-INF');
if (!fs.existsSync(metaInfDir)) {
    fs.mkdirSync(metaInfDir);
}

// Create MANIFEST.MF
const manifest = `Manifest-Version: 1.0
Created-By: ModelHunt Plugin Builder
Main-Class: com.modelhunt.ModelHuntPlugin

`;

fs.writeFileSync(path.join(metaInfDir, 'MANIFEST.MF'), manifest);

// Create the JAR file (as a ZIP since we don't have jar command)
console.log('Creating JAR file...');

// Create a simple JAR structure by copying files
const jarPath = path.join(targetDir, 'ModelHuntPlugin-1.0.0.jar');

// Since we can't compile Java in WebContainer, create a source JAR instead
const sourceJarDir = path.join(targetDir, 'source-jar');
if (!fs.existsSync(sourceJarDir)) {
    fs.mkdirSync(sourceJarDir, { recursive: true });
}

// Copy source files
copyDir('src', sourceJarDir);
copyDir(resourcesDir, path.join(sourceJarDir, 'resources'));

// Create a README for the source JAR
const sourceReadme = `# ModelHunt Plugin Source

This is the source code for the ModelHunt plugin.

## To build on your server:

1. Ensure you have Maven installed
2. Run: mvn clean package
3. The JAR file will be created in the target directory

## Dependencies Required:
- Paper 1.21.4
- ModelEngine
- MythicMobs
- PlaceholderAPI (optional)
- Vault (optional)

## Installation:
1. Place the built JAR in your plugins folder
2. Restart your server
3. Configure using /hunt gui command
`;

fs.writeFileSync(path.join(targetDir, 'BUILD-INSTRUCTIONS.txt'), sourceReadme);

console.log('✅ Plugin source prepared!');
console.log('📁 Source files ready in target/source-jar/');
console.log('📋 Build instructions created in target/BUILD-INSTRUCTIONS.txt');
console.log('');
console.log('⚠️  Note: Java compilation requires Maven on your server.');
console.log('   Copy the source files to a system with Maven to build the JAR.');