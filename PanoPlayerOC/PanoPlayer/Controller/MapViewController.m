//
//  MapViewController.m
//  PanoPlayer
//
//  Created by 李文博 on 13-1-18.
//  Copyright (c) 2013年 __MyCompanyName__. All rights reserved.
//

#import "MapViewController.h"
#import "PlayerViewController.h"
#import "ASIHTTPRequest.h"
#import "ASIDownloadCache.h"

@interface MapViewController ()

@end

@implementation MapViewController
{
    UIScrollView         *_viewScrollStub;
    MTImageMapView       *_viewImageMap;
    NSArray              *_stateNames;
}
@synthesize viewScrollStub  = _viewScrollStub;
@synthesize viewImageMap    = _viewImageMap;
@synthesize stateNames      = _stateNames;
@synthesize linkScene;
@synthesize coordsData;
@synthesize responseData;
@synthesize imageProgressIndicator;

-(NSString *)nibName
{
    return @"MapViewController";
}

- (void)loadView
{
    [super loadView];
    
    //UIImage *mapImage = [UIImage imageWithContentsOfFile:@"map.gif"];
    //_viewImageMap.image = mapImage;
    //[_viewImageMap sizeToFit];
    coordsData = [[NSArray alloc] init];
    
    [_viewScrollStub addSubview:_viewImageMap];
    [_viewScrollStub setContentSize:[_viewImageMap sizeThatFits:CGSizeZero]];
    
    
    UILabel *loading = [[[UILabel alloc] initWithFrame:CGRectZero] autorelease];
    loading.text = @"地图加载中...";
    loading.backgroundColor = [UIColor clearColor];
    loading.font = [UIFont fontWithName:@"Arial" size:12];
    loading.textAlignment = UITextAlignmentCenter;
    //loading.font
    [_viewScrollStub addSubview:loading];
    
    CGSize result = [[UIScreen mainScreen] bounds].size;
    int height = result.height;
    int width = result.width;
    int progressWdith = width/2;
    int x = (width - 90)/2;
    int y = (height/2)-100;
    [loading setFrame:CGRectMake(x,y,90,20)];
    
    
    imageProgressIndicator = [[[UIProgressView alloc] initWithFrame:CGRectZero] autorelease];
    [_viewScrollStub addSubview:imageProgressIndicator];
    y = y+20;
    x = (width - progressWdith)/2;
    [imageProgressIndicator setFrame:CGRectMake(x,y,progressWdith,30)];
    
    
    
    responseData = [[NSString alloc] init];
    NSString *panoInfoUrl = [NSString stringWithFormat:@"http://beta1.yiluhao.com/ajax/m/pm/id/%@",  @"1"];
    //NSLog(@"panoInfoUrl=%@", panoInfoUrl);
    responseData = [self getPanoMapFromUrl:panoInfoUrl];
    //}
    Boolean flag = false;
    //NSLog(@"na%@", responseData);
    if(responseData !=nil){
        NSDictionary *resultsDictionary = [responseData objectFromJSONString];
        coordsData = [resultsDictionary objectForKey:@"coords"];
        //NSLog(@"na%@", coordsData);
        NSString *mapUrl = [resultsDictionary objectForKey:@"map"];
        
        if(coordsData && mapUrl){
            [self downLoadImage:mapUrl];
        }
        
        flag = true;
    }
    if(!flag){
        [self getWrong:@"加载数据出错"];
    }
    
}
- (void) getWrong:(NSString*)str{
    NSString *msg = [NSString stringWithFormat:@"%@", str];
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil message:msg delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil, nil];
    [alert show];
    [alert release];
}

-(NSString *)getPanoMapFromUrl:(NSString *)url{
    if(url == nil){
        [self getWrong:@"参数错误"];
        return @"";
    }
    //NSString *responseData = [[NSString alloc] init];
    
    ASIDownloadCache *cache = [[ASIDownloadCache alloc] init];
    NSString *cachePath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    [cache setStoragePath:[cachePath stringByAppendingPathComponent:@"resource"]];
    
    
    ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:url]];
    
    
    [request setDownloadCache:cache];
    
    [request setCacheStoragePolicy:ASIAskServerIfModifiedWhenStaleCachePolicy];
    [cache setShouldRespectCacheControlHeaders:NO];
    //[]
    [request setSecondsToCache:60*60*24*30*10]; //30
    
    [request startSynchronous];
    
    NSError *error = [request error];
    if (!error) {
        responseData = [request responseString];
        //NSLog(@"response%@", responseData);
    }
    else {
        [self getWrong:@"获取数据失败"];
    }
    
    return responseData;
}

-(void)downLoadImage:(NSString *)url{
    //return;
    //url = @"http://beta1.yiluhao.com/html/pano-6000.jpg";
    
    if(url == nil){
        [self getWrong:@"加载地图失败"];
        return;
    }
    
    ASIDownloadCache *cache = [[ASIDownloadCache alloc] init];
    NSString *cachePath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    [cache setStoragePath:[cachePath stringByAppendingPathComponent:@"resource"]];
    [cache setShouldRespectCacheControlHeaders:NO] ;
    
    ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:url]];
    
    [request setDownloadCache:cache];
    
    [request setCacheStoragePolicy:ASIAskServerIfModifiedWhenStaleCachePolicy];
    [request setDownloadProgressDelegate:imageProgressIndicator];
    [request setSecondsToCache:60*60*24*30*10]; //30
    
    [request setUserInfo:[NSDictionary dictionaryWithObject:coordsData forKey:@"coords"]];
    
    [request setDelegate : self ];
    Boolean cached = [cache isCachedDataCurrentForRequest:request];
    if(cached){
        [imageProgressIndicator setProgress:100];
        NSLog(@"cached");
    }

    [request startAsynchronous];

}
- ( void )requestFinished:( ASIHTTPRequest *)request
{
    //[imageProgressIndicator setProgress:100000 animated:YES];
    NSArray *coords = [[request userInfo] objectForKey:@"coords"];
	UIImage *img = [UIImage imageWithData:[request responseData]];
	if (img) {
		[self displayMap:img coords:coords];
	}
    else{
        [self getWrong:@"下载地图失败"];
    }
}

- ( void )requestFailed:( ASIHTTPRequest *)request
{
    if ([[request error] domain] != NetworkRequestErrorDomain || [[request error] code] != ASIRequestCancelledErrorType) {
        [self getWrong:@"下载地图出错，请检查您的网络"];
    }
}


-(void)displayMap:(UIImage *)mapImage coords:(NSArray *)coords{
    //NSLog(@"cccccc");
    //NSLog(@"coordsData=%@", coordsData);
    //return;
    NSMutableArray *arrStates = [[NSMutableArray alloc] init];
    linkScene = [[NSMutableArray alloc] init];
    UIImage *logo = [UIImage imageNamed:@"camera1.png"];

    for (int i=0; i<coordsData.count; i++) {
        NSDictionary  *tmp = [coordsData objectAtIndex:i];
        NSString *coords = [tmp objectForKey:@"coords"];
        NSString *linkId = [tmp objectForKey:@"linkScene"];
        [arrStates addObject:coords];
        [linkScene addObject:linkId];
        NSArray *aArray = [coords componentsSeparatedByString:@","];
        float marginLeft = [[aArray objectAtIndex:0] floatValue];
        float marginTop = [[aArray objectAtIndex:1] floatValue];
        //if(i==3){
        //添加水印
        mapImage = [self addImageLogo:mapImage waterMark:logo left:marginLeft top:marginTop];
        //}
    }
    [logo release], logo=nil;
    
    _viewImageMap.image = mapImage;
    [_viewImageMap sizeToFit];
    
    [_viewScrollStub addSubview:_viewImageMap];
    
    
    [_viewScrollStub setContentSize:
     [_viewImageMap sizeThatFits:CGSizeZero]
	 ];
    
    [_viewImageMap
     setMapping:arrStates
     doneBlock:^(MTImageMapView *imageMapView) {
         NSLog(@"Areas are all mapped");
     }];
}

-(UIImage *)addImageLogo:(UIImage *)img waterMark:(UIImage *)logo left:(float)left top:(float)top{
    //get image width and height
    int w = img.size.width;
    int h = img.size.height;
    int logoWidth = logo.size.width;
    int logoHeight = logo.size.height;
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    
    //create a graphic context with CGBitmapContextCreate
    CGContextRef context = CGBitmapContextCreate(NULL, w, h, 8, 4 * w, colorSpace, kCGImageAlphaPremultipliedFirst);
    CGContextDrawImage(context, CGRectMake(0, 0, w, h), img.CGImage);
    
    CGContextDrawImage(context, CGRectMake(left+15.0f, h-top-25.0f-logoHeight, logoWidth, logoHeight), [logo CGImage]);
    
    CGImageRef imageMasked = CGBitmapContextCreateImage(context);
    CGContextRelease(context);
    CGColorSpaceRelease(colorSpace);
    return [UIImage imageWithCGImage:imageMasked];
}


-(void)imageMapView:(MTImageMapView *)inImageMapView
   didSelectMapArea:(NSUInteger)inIndexSelected
{
    //NSDictionary *panoInfo = [panoList objectAtIndex:indexPath.row];
    NSString *panoId = [linkScene objectAtIndex:inIndexSelected];
    //NSLog(@"idddd=%@", panoId);
    
    PlayerViewController *playerView = [[PlayerViewController alloc] init];
    playerView.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:playerView animated:YES];
    
    NSDictionary *dic=[[NSDictionary alloc] initWithObjectsAndKeys:panoId,@"panoId", nil];
    //NSDictionary
    
    //发送消息.@"pass"匹配通知名，object:nil 通知类的范围
    [[NSNotificationCenter defaultCenter] postNotificationName:@"panoId" object:nil userInfo:dic];
    
    playerView.title = @"player";
    
    [playerView release];
}



- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

@end