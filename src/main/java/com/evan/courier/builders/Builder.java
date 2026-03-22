package com.evan.courier.builders;

import java.io.IOException;

public interface Builder {
  /**
   * Builds and returns the HTML content for this widget.
   *
   * @return a string containing the rendered HTML for the widget
   * @throws IOException if fetching external data or reading template resources fails
   */
  String build() throws IOException;
}
