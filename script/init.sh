#!/usr/bin/env bash
#
# © 2026-present https://github.com/cengiz-pz
#

set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(realpath "$SCRIPT_DIR/..")

plugin_node_name=""
dry_run=false

# Portable in-place sed handling
sed_cmd=(sed)
sed_inplace=(-i)
if ! sed --version >/dev/null 2>&1; then
	# BSD/macOS sed requires an empty extension argument
	sed_inplace=(-i "")
fi

function display_help()
{
	echo
	$SCRIPT_DIR/echocolor.sh -y "The " -Y "$0 script" -y " renames plugin template files and content"
	echo
	$SCRIPT_DIR/echocolor.sh -Y "Syntax:"
	$SCRIPT_DIR/echocolor.sh -y "	$0 [-h|n <plugin node name>|-d]"
	echo
	$SCRIPT_DIR/echocolor.sh -Y "Options:"
	$SCRIPT_DIR/echocolor.sh -y "	h	display usage information"
	$SCRIPT_DIR/echocolor.sh -y "	n	specify name of the plugin node (eg. ConnectionState)"
	$SCRIPT_DIR/echocolor.sh -y "	d	dry-run mode; show what would be done without making changes"
	echo
	$SCRIPT_DIR/echocolor.sh -Y "Examples:"
	$SCRIPT_DIR/echocolor.sh -y "	* Create a OneStopShop plugin"
	$SCRIPT_DIR/echocolor.sh -y "		$> $0 -n OneStopShop"
	$SCRIPT_DIR/echocolor.sh -y "	* Dry-run for OneStopShop plugin"
	$SCRIPT_DIR/echocolor.sh -y "		$> $0 -n OneStopShop -d"
	echo
}


function display_status()
{
	echo
	$SCRIPT_DIR/echocolor.sh -c "********************************************************************************"
	$SCRIPT_DIR/echocolor.sh -c "* $1"
	$SCRIPT_DIR/echocolor.sh -c "********************************************************************************"
	echo
}


function display_error()
{
	$SCRIPT_DIR/echocolor.sh -r "Error: $1"
}


function split_caps() {
	local input="$1"
	local formatted

	# 1. Insert spaces between lowercase followed by uppercase
	# 2. Insert spaces between uppercase followed by Uppercase+lowercase (start of new word)
	formatted=$(sed -E 's/([[:lower:]])([[:upper:]])/\1 \2/g; s/([[:upper:]])([[:upper:]][[:lower:]])/\1 \2/g' <<< "$input")

	echo "$formatted"
}


replace_string() {
	# replace_string <dry_run> <directory> <target> <replacement>
	local dry_run="$1"
	local root_dir="$2"
	local target="$3"
	local replacement="$4"

	# Escape target and replacement for sed
	local target_esc=$(printf '%s' "$target" | sed 's|[\&/\\]|\\&|g')
	local repl_esc=$(printf '%s' "$replacement" | sed 's|[\&/\\]|\\&|g')
	local sed_expr="s/${target_esc}/${repl_esc}/g"

	# Find files that actually contain the target string
	local matching_files
	matching_files=$(find "$root_dir" -type f \
		-not -path "$root_dir/.git/*" \
		-not -path "$root_dir/script/*" \
		-not -path "$root_dir/demo/addons/*" \
		-not -path "$root_dir/ios/godot/*" \
		-not -path "$root_dir/ios/Pods/*" \
		-not -iname "*.png" \
		-not -iname "*.jar" \
		-not -iname "*.zip" \
		-not -iname ".DS_Store" \
		-exec grep -l -F "$target" {} + || true)

	if [[ -z "$matching_files" ]]; then
		echo "No occurrences of '$target' found – skipping."
		return
	fi

	if $dry_run; then
		echo "Would replace '$target' → '$replacement' in the following files:"
	else
		echo "Replacing '$target' → '$replacement' in the following files:"
	fi

	printf '%s\n' "$matching_files" | sed 's/^/  /'
	echo

	if $dry_run; then
		return
	fi

	# Perform the actual replacement
	find "$root_dir" -type f \
		-not -path "$root_dir/.git/*" \
		-not -path "$root_dir/script/*" \
		-not -path "$root_dir/demo/addons/*" \
		-not -path "$root_dir/ios/godot/*" \
		-not -path "$root_dir/ios/Pods/*" \
		-not -iname "*.png" \
		-not -iname "*.jar" \
		-not -iname "*.zip" \
		-not -iname ".DS_Store" \
		-exec env LC_ALL=C "${sed_cmd[@]}" "${sed_inplace[@]}" -e "$sed_expr" {} +
}

rename_template() {
	# rename_template <dry_run> <root_dir> <target> <replacement>
	local dry_run="$1"
	local root_dir="$2"
	local target="$3"
	local replacement="$4"

	root_dir=$(realpath -- "$root_dir")

	local root_realpath=$(realpath -- "$root_dir")

	local to_rename=()

	while IFS= read -r -d '' file; do
		if [[ "$(realpath -- "$file")" == "$root_realpath" ]]; then
			continue
		fi

		local dir_name=$(dirname -- "$file")
		local base_name=$(basename -- "$file")
		local new_base_name="${base_name//$target/$replacement}"

		if [[ "$base_name" == "$new_base_name" ]]; then
			continue
		fi

		local new_path="${dir_name}/${new_base_name}"
		to_rename+=("$file:$new_path")
	done < <(find "$root_dir" -depth -name "*$target*" \
		-not -path "$root_dir/.git/*" \
		-not -path "$root_dir/script/*" \
		-not -path "$root_dir/demo/addons/*" \
		-not -path "$root_dir/ios/godot/*" \
		-not -path "$root_dir/ios/Pods/*" \
		-print0)

	if [[ ${#to_rename[@]} -eq 0 ]]; then
		return
	fi

	echo "Renaming files/directories containing '$target' → '$replacement':"

	for item in "${to_rename[@]}"; do
		IFS=':' read -r old_path new_path <<< "$item"
		if $dry_run; then
			echo "  would rename '$old_path' -> '$new_path'"
		else
			mv -- "$old_path" "$new_path"
			echo "  renamed '$old_path' -> '$new_path'"
		fi
	done

	echo
}

while getopts "hn:d" option; do
	case $option in
		h)
			display_help
			exit;;
		n)
			plugin_node_name=$OPTARG
			;;
		d)
			dry_run=true
			;;
		\?)
			display_error "Invalid option: $option"
			echo
			display_help
			exit;;
	esac
done

if [[ -z "$plugin_node_name" ]]; then
	display_error "Plugin node name not specified"
	display_help
	exit 1
fi

if ! [[ "$plugin_node_name" =~ ^[A-Za-z][A-Za-z0-9]*$ ]]; then
	display_error "Plugin node name must start with a letter and contain only alphanumeric characters"
	exit 1
fi

if $dry_run; then
	display_status "Dry-run mode enabled: No changes will be made"
fi


read -ra node_name_parts <<< "$(split_caps "$plugin_node_name")"
display_status "Replacing 'PluginTemplate' with '$plugin_node_name'"
replace_string "$dry_run" "$ROOT_DIR" "PluginTemplate" "$plugin_node_name"
echo
rename_template "$dry_run" "$ROOT_DIR" "PluginTemplate" "$plugin_node_name"


lowercase_plugin_node_name=$(printf '%s' "$plugin_node_name" | tr '[:upper:]' '[:lower:]')
display_status "Replacing 'plugintemplate' with '$lowercase_plugin_node_name'"
replace_string "$dry_run" "$ROOT_DIR" "plugintemplate" "$lowercase_plugin_node_name"
echo
rename_template "$dry_run" "$ROOT_DIR" "plugintemplate" "$lowercase_plugin_node_name"


joined_string=$(IFS='_'; echo "${node_name_parts[*]}")
lowercase_joined_string=$(printf '%s' "$joined_string" | tr '[:upper:]' '[:lower:]')
display_status "Replacing 'plugin_template' with '$lowercase_joined_string'"
replace_string "$dry_run" "$ROOT_DIR" "plugin_template" "$lowercase_joined_string"
echo
rename_template "$dry_run" "$ROOT_DIR" "plugin_template" "$lowercase_joined_string"


joined_string=$(IFS='-'; echo "${node_name_parts[*]}")
lowercase_joined_string=$(printf '%s' "$joined_string" | tr '[:upper:]' '[:lower:]')
display_status "Replacing 'plugin-template' with '$lowercase_joined_string'"
replace_string "$dry_run" "$ROOT_DIR" "plugin-template" "$lowercase_joined_string"
echo
rename_template "$dry_run" "$ROOT_DIR" "plugin-template" "$lowercase_joined_string"


joined_string=$(IFS=' '; echo "${node_name_parts[*]}")
display_status "Replacing 'Plugin Template' with '$joined_string'"
replace_string "$dry_run" "$ROOT_DIR" "Plugin Template" "$joined_string"


display_status "Removing initialization section from README doc"
if ! $dry_run; then
	echo "Removing initialization section in $ROOT_DIR/docs/README.md"
	"${sed_cmd[@]}" "${sed_inplace[@]}" '/<!--TO-BE-DELETED-AFTER-INIT-BEGIN-->/,/<!--TO-BE-DELETED-AFTER-INIT-END-->/d' "$ROOT_DIR/docs/README.md"
else
	echo "Would remove initialization section in $ROOT_DIR/docs/README.md"
fi
echo


display_status "Initialization completed; self-destructing"
if ! $dry_run; then
	rm -v "$0"
else
	echo "Would remove \"$0\""
fi
