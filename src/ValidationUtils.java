import java.util.regex.Pattern;

public class ValidationUtils {
    public static final String IPV4_PATTERN_NON_EMPTY =
            "\\b((25[0-5]|2[0-4]\\d|[01]\\d\\d|\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|[01]\\d\\d|\\d?\\d)";
    // IPv4 pattern should not match the empty string because 'no entry' is represented in engine with null
    public static final String IPV4_PATTERN = "^" + IPV4_PATTERN_NON_EMPTY;
    private static final String IPV6_ADDRESS_BLOCK = "[0-9a-fA-F]{1,4}";
    private static final String IPV6_HEX_COMPRESSED_PATTERN =
            "((?:" + IPV6_ADDRESS_BLOCK + "(?::" + IPV6_ADDRESS_BLOCK + ")*)?)::((?:" +
                    IPV6_ADDRESS_BLOCK + "(?::" + IPV6_ADDRESS_BLOCK + ")*)?)";
    private static final String IPV6_STD_PATTERN = "(?:" + IPV6_ADDRESS_BLOCK + ":){7}" + IPV6_ADDRESS_BLOCK;
    public static final String IPV6_PATTERN = "(?:" + IPV6_STD_PATTERN + "|" + IPV6_HEX_COMPRESSED_PATTERN + ")";
    public static final String IPV4_OR_IPV6_PATTERN = IPV4_PATTERN + "|" + IPV6_PATTERN;

    public static final Pattern ipv6RegexPattern = Pattern.compile(IPV6_PATTERN);
    public static final Pattern ipv4RegexPattern = Pattern.compile(IPV4_PATTERN);


    /**
     *
     * @param ipv4Address a ipv4 address with prefix ('/24') is invalid
     * @return true if the address matches the regex
     */
    public static boolean isValidIpv4(String ipv4Address) {
        return ipv4Address == null ? false : ipv4RegexPattern.matcher(ipv4Address).matches();
    }

    /**
     *
     * @param ipv6Address an address with prefix ('/64') or link local zone index ('%eth0')
     *                    is invalid
     * @return true if the address matches the regex
     */
    public static boolean isValidIpv6(String ipv6Address) {
        return ipv6Address == null ? false : ipv6RegexPattern.matcher(ipv6Address).matches();
    }
}