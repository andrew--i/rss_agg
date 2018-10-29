package org.ai.controller;

import org.ai.view.FeedView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.View;

@RestController
public class FeedController {

    @Autowired
    private FeedView view;

    @GetMapping(value = "/feed")
    public View getFeed() {
        return view;
    }
}
