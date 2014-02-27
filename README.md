exonian
=======

The Exonian
CSC426 Project Proposal

Team

Alec Sun
Weihang Fan


Goals

Scrape the articles from the Exonian website front page using Android’s JSON parser.
Display the articles in categories in the app by creating swipeable tabs.
Create a local database on each device to allow fast access to articles.
Enable navigation between components of the app, such as between HomeActivity and ArticleActivity.
Write code that is commented, formatted, readable, and debuggable. Use GitHub to share code and version control.
Display the articles and homepage in a visually appealing style.
Enable sharing the article onto social networking sites such as Facebook and Twitter.


Timeline

2014.02.17: Successfully scrape data and show articles with title, author, date, and category in an activity. Make the individual article format visually appealing.

2014.02.21: Add a database to store scraped articles and create a search function by author or title to easily find articles.

2014.02.21: Make a homepage with buttons to navigate to individual articles and categories as tabs. Make the homepage visually appealing.

2014.02.24: Add social networking on sites like Facebook and Twitter to share the articles.

2014.02.26: Make sure all code is commented. Test on tablet and phone. Write a users’ manual.


Resources

Android JSON Parsing 
A nice tutorial to parse JSON from websites for Android is available here: http://www.vogella.com/tutorials/AndroidJSON/article.html. To get the JSON as a string from http://theexonian.com we add /?json=1 to the end of the URL (according to the JSON plugin by WordPress).

Scraping Images
The tutorial http://theopentutorials.com/tutorials/android/imageview/android-how-to-load-image-from-url-in-imageview/ helped us retrieve images given their URL and place them into ImageView for an article.

Connecting to the Network
The Android developer training tutorial located at
http://developer.android.com/training/basics/network-ops/connecting.html helped us program a asynchronous HTTP request schema.

ListView
The Vogella tutorial located at http://www.vogella.com/tutorials/AndroidListView/article.html helped us create a custom list adapter and display the articles in a ListView on the HomeActivity.

SearchView
The Android developer training tutorial located at http://developer.android.com/guide/topics/search/search-dialog.html helped us with program searching for articles.

Regular Expressions
The site http://www.regular-expressions.info/characters.html taught us how to use regular expressions to escape apostraphes in the article titles which were showing up as &#8217; and &#8220 in the JSON.

JSON Formatter
For our convenience we pasted the JSON into http://www.freeformatter.com/json-formatter.html#ad-output to get the format of The Exonian website JSON so we could easily get the title, content, and url of each article.

Sharing Articles
A feature we want our app to do is be able to share the links of articles. This is done at http://developer.android.com/training/sharing/send.html and http://developer.android.com/training/sharing/shareaction.html, where Android teaches us how to use the ShareIntent that apps including Facebook and Mail all support.

