#!/usr/bin/env bash
# ch22-release/iosApp/archive.sh
# Release archive of the iOS app. Needs macOS with Xcode 26+ and XcodeGen.
#   APPLE_TEAM_ID=YOUR_TEAM_ID ./archive.sh   signed archive + export for upload
#   UNSIGNED=1 ./archive.sh                   unsigned archive, no export
set -euo pipefail
cd "$(dirname "$0")"
xcodegen generate

if [ "${UNSIGNED:-0}" = 1 ]; then
  signing=(CODE_SIGNING_ALLOWED=NO)
else
  signing=(DEVELOPMENT_TEAM="${APPLE_TEAM_ID:?set APPLE_TEAM_ID}"
           -allowProvisioningUpdates)
fi

# Configuration Release makes the Gradle run script link the release framework.
xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Release \
  -destination 'generic/platform=iOS' -archivePath build/iosApp.xcarchive \
  "${signing[@]}" archive

if [ "${UNSIGNED:-0}" != 1 ]; then
  xcodebuild -exportArchive -archivePath build/iosApp.xcarchive \
    -exportPath build/export -exportOptionsPlist ExportOptions.plist \
    -allowProvisioningUpdates
fi
