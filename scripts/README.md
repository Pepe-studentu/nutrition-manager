# User Update Scripts

## For End Users (Non-Technical)

### `update-app.bat`
**Double-click to update the Nutrition App**

This script:
1. Downloads the latest version from GitHub
2. Installs it to Program Files
3. Preserves all user data in `~/NutritionApp/`
4. Creates a desktop shortcut
5. Optionally launches the updated app

**Important**:
- User data (foods, meals, templates, PDFs) remains safe in `~/NutritionApp/`
- No technical knowledge required - just double-click
- Requires internet connection

## For Developer (You)

### Setup Instructions

1. **Update the script configuration** in `update-app.bat`:
   ```batch
   set GITHUB_USER=YourGitHubUsername
   set GITHUB_REPO=NutritionApp
   set APP_NAME=KotlinProjectTest
   ```

2. **Create GitHub releases**:
   - Build your app: `./gradlew packageDistributionForCurrentOS`
   - Create a new release on GitHub
   - Upload the executable/zip file
   - Tag it appropriately (e.g., v1.0.0, v1.1.0)

3. **Distribute the script**:
   - Give users the `update-app.bat` file once
   - They can reuse it for all future updates
   - Include it with initial app distribution

### Alternative: Simple EXE Distribution

If you prefer not to use the update script:
- Just send the new EXE file to users
- User data in `~/NutritionApp/` will be preserved automatically
- Users replace the old EXE with the new one

## Data Safety

Both approaches ensure:
- ✅ User data persists across updates
- ✅ Templates and settings preserved
- ✅ PDF exports remain accessible
- ✅ No risk of data loss during updates