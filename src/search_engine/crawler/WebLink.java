package search_engine.crawler;

/**
 * Web link contains a href to a webpage as well as its depth compared to the
 * root href
 * 
 * @author ngtrhieu0011
 * 
 */
class WebLink {
	private String _link;
	private int _depth;
	
	public WebLink (String link, int depth) {
		_link = link;
		_depth = depth;
	}
	
	public String getLink () {
		return _link;
	}
	
	public int getDepth () {
		return _depth;
	}
}
