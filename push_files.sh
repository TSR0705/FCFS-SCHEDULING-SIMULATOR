#!/bin/bash

# Script to push all remaining files to GitHub repo, one file per commit

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting to push all remaining files to GitHub repo...${NC}"

# Function to commit and push a single file
commit_and_push_file() {
    local file="$1"
    local commit_message="$2"
    
    # Check if file has changes
    if git diff --quiet "$file" && git diff --cached --quiet "$file"; then
        echo -e "${YELLOW}Skipping $file (no changes)${NC}"
        return 0
    fi
    
    # Add file
    git add "$file"
    
    # Commit file
    git commit -m "$commit_message"
    
    # Push file
    if git push origin main; then
        echo -e "${GREEN}Successfully pushed $file${NC}"
    else
        echo -e "${RED}Failed to push $file${NC}"
        return 1
    fi
}

# Find all files except ignored ones
find . -type f \
    -not -path "./target/*" \
    -not -path "./out/*" \
    -not -path "./build/*" \
    -not -path "./.idea/*" \
    -not -path "./.vscode/*" \
    -not -path "./node_modules/*" \
    -not -name ".DS_Store" \
    -not -name "*.class" \
    -not -name "push_files.sh" \
    -not -name "README.md" | sort | while read -r file; do
    
    # Get filename and extension
    filename=$(basename "$file")
    extension="${filename##*.}"
    basename_no_ext="${filename%.*}"
    
    # Generate commit message based on file type
    case "$extension" in
        java)
            if [[ "$filename" == "module-info.java" ]]; then
                commit_msg="Add module descriptor"
            else
                commit_msg="Add $basename_no_ext"
            fi
            ;;
        png|jpg|jpeg|gif)
            commit_msg="Add screenshot $filename"
            ;;
        md)
            commit_msg="Add/update documentation $filename"
            ;;
        xml)
            if [[ "$filename" == "pom.xml" ]]; then
                commit_msg="Add Maven project configuration"
            else
                commit_msg="Add configuration file $filename"
            fi
            ;;
        fxml)
            commit_msg="Add FXML view $filename"
            ;;
        cmd|sh)
            commit_msg="Add script $filename"
            ;;
        *)
            commit_msg="Add $filename"
            ;;
    esac
    
    echo -e "${YELLOW}Processing $file${NC}"
    commit_and_push_file "$file" "$commit_msg"
done

echo -e "${GREEN}Finished pushing all files!${NC}"