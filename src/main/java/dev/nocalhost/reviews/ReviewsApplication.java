package dev.nocalhost.reviews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@Controller
public class ReviewsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewsApplication.class, args);
	}

	private final static Boolean ratings_enabled = Boolean.valueOf(System.getenv("ENABLE_RATINGS"));
	private final static String star_color = System.getenv("STAR_COLOR") == null ? "black" : System.getenv("STAR_COLOR");
	private final static String services_domain = System.getenv("SERVICES_DOMAIN") == null ? "" : ("." + System.getenv("SERVICES_DOMAIN"));
	private final static String ratings_hostname = System.getenv("RATINGS_HOSTNAME") == null ? "ratings" : System.getenv("RATINGS_HOSTNAME");
	private final static String ratings_service = "http://" + ratings_hostname + services_domain + ":9080/ratings";
	// HTTP headers to propagate for distributed tracing are documented at
	// https://istio.io/docs/tasks/telemetry/distributed-tracing/overview/#trace-context-propagation
	private final static String[] headers_to_propagate = {
			// All applications should propagate x-request-id. This header is
			// included in access log statements and is used for consistent trace
			// sampling and log sampling decisions in Istio.
			"x-request-id",

			// Lightstep tracing header. Propagate this if you use lightstep tracing
			// in Istio (see
			// https://istio.io/latest/docs/tasks/observability/distributed-tracing/lightstep/)
			// Note: this should probably be changed to use B3 or W3C TRACE_CONTEXT.
			// Lightstep recommends using B3 or TRACE_CONTEXT and most application
			// libraries from lightstep do not support x-ot-span-context.
			"x-ot-span-context",

			// Datadog tracing header. Propagate these headers if you use Datadog
			// tracing.
			"x-datadog-trace-id",
			"x-datadog-parent-id",
			"x-datadog-sampling-priority",

			// W3C Trace Context. Compatible with OpenCensusAgent and Stackdriver Istio
			// configurations.
			"traceparent",
			"tracestate",

			// Cloud trace context. Compatible with OpenCensusAgent and Stackdriver Istio
			// configurations.
			"x-cloud-trace-context",

			// Grpc binary trace context. Compatible with OpenCensusAgent nad
			// Stackdriver Istio configurations.
			"grpc-trace-bin",

			// b3 trace headers. Compatible with Zipkin, OpenCensusAgent, and
			// Stackdriver Istio configurations. Commented out since they are
			// propagated by the OpenTracing tracer above.
			"x-b3-traceid",
			"x-b3-spanid",
			"x-b3-parentspanid",
			"x-b3-sampled",
			"x-b3-flags",

			// Application-specific headers to forward.
			"end-user",
			"user-agent",
	};

	private String getJsonResponse (String productId, int starsReviewer1, int starsReviewer2) {
		String result = "{";
		result += "\"id\": \"" + productId + "\",";
		result += "\"reviews\": [";

		// reviewer 1:
		result += "{";
		result += "  \"reviewer\": \"Reviewer1\",";
		result += "  \"text\": \"An extremely entertaining play by Shakespeare. The slapstick humour is refreshing!\"";
		if (ratings_enabled) {
			if (starsReviewer1 != -1) {
				result += ", \"rating\": {\"stars\": " + starsReviewer1 + ", \"color\": \"" + star_color + "\"}";
			}
			else {
				result += ", \"rating\": {\"error\": \"Ratings service is currently unavailable\"}";
			}
		}
		result += "},";

		// reviewer 2:
		result += "{";
		result += "  \"reviewer\": \"Reviewer2\",";
		result += "  \"text\": \"Absolutely fun and entertaining. The play lacks thematic depth when compared to other plays by Shakespeare.\"";
		if (ratings_enabled) {
			if (starsReviewer2 != -1) {
				result += ", \"rating\": {\"stars\": " + starsReviewer2 + ", \"color\": \"" + star_color + "\"}";
			}
			else {
				result += ", \"rating\": {\"error\": \"Ratings service is currently unavailable\"}";
			}
		}
		result += "}";

		result += "]";
		result += "}";

		return result;
	}

	@RequestMapping(value = "/health", method = RequestMethod.GET)
	@ResponseBody
	public String health() {
		return "{\"status\": \"Reviews is healthy\"}";
	}

	@RequestMapping(value = "/reviews/{productId}", method = RequestMethod.GET)
	@ResponseBody
	public String bookReviewsById(@PathVariable("productId") int productId) {
		int starsReviewer1 = -1;
		int starsReviewer2 = -1;

		return getJsonResponse(Integer.toString(productId), starsReviewer1, starsReviewer2);
	}

}