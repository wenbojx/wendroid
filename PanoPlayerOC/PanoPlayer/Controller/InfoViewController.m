//
//  InfoViewController.m
//  PanoPlayer
//
//  Created by 李文博 on 13-1-18.
//  Copyright (c) 2013年 __MyCompanyName__. All rights reserved.
//

#import "InfoViewController.h"

@interface InfoViewController ()

@end

@implementation InfoViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    iWebView = [[UIWebView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    [self.view addSubview:iWebView];
    [iWebView setBackgroundColor:[UIColor clearColor]];
    
    UIView *header = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 50)];
    [header setBackgroundColor:[UIColor clearColor]];
    
    [self loadDocument:@"info.html"];
    
}
//加载本地使用说明文件文件
-(void)loadDocument:(NSString *)docName
{
    NSString *mainBundleDirectory=[[NSBundle mainBundle] bundlePath];
    NSString *path=[mainBundleDirectory stringByAppendingPathComponent:docName];
    
    NSURL *url=[NSURL fileURLWithPath:path];
    NSURLRequest *request=[NSURLRequest requestWithURL:url];
    iWebView.scalesPageToFit=YES;
    [iWebView loadRequest:request];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

@end
