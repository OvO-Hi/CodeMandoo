require_relative '../node_modules/react-native/scripts/react_native_pods'
cli_cmd=[File.expand_path('../node_modules/.bin/react-native', __dir__), 'config']
result = list_native_modules!(cli_cmd)
puts result.inspect
